'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

/**
 * Triggers when a new message is sent to an event chat and sends notifications.
 *
 * Messages add a flag to `/Messages/{eventId}`.
 * Users save their device notification tokens to `/Messages/{eventId}/notificationTokens/{deviceToken}`.
 */
exports.sendMessageNotification = functions.database.ref('/Messages/{eventId}/{messageId}/').onWrite(event => {
  const eventId = event.params.eventId;
  const messageId = event.params.messageId;
  var message;
  admin.database().ref(`/Messages/${eventId}/${messageId}/message`).on("value",
    function(snapshot) {
      console.log("Message: ", snapshot.val());
      message = snapshot.val();
    }, function (errorObject) {
      console.log("The read failed: " + errorObject.code);
  });
  
  console.log('We have a new message for event: ', eventId, ', message: ', message);

  // Get the list of device notification tokens.
  const getDeviceTokensPromise = admin.database().ref(`/Messages/${eventId}/notificationTokens`).once('value');

  return Promise.all([getDeviceTokensPromise]).then(results => {
    const tokensSnapshot = results[0];
    const message = results[0];

    // Check if there are any device tokens.
    if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');

    // Notification details.
    const payload = {
      notification: {
        title: 'Ready2Meet',
        body: message.message
      }
    };

    // Listing all tokens.
    const tokens = Object.keys(tokensSnapshot.val());

    // Send notifications to all tokens.
    return admin.messaging().sendToDevice(tokens, payload).then(response => {
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
    });
  });
});
