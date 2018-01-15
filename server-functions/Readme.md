# Send Firebase Cloud Messaging notifications if new messages are added to the chat

## Deploy and test

This sample comes with a web-based UI for testing the function. To test it out:

 1. Create a Firebase Project using the [Firebase Console](https://console.firebase.google.com).
 1. Enable **Google Provider** in the [Auth section](https://console.firebase.google.com/project/_/authentication/providers)
 1. Clone or download this repo and open the `fcm-notification` directory.
 1. You must have the Firebase CLI installed. If you don't have it install it with `npm install -g firebase-tools` and then configure it with `firebase login`.
 1. Configure the CLI locally by using `firebase use --add` and select your project in the list.
 1. Install dependencies locally by running: `cd functions; npm install`
 1. Deploy your project using `firebase deploy`


Note: The code is based on [FCM Notifications of the Google samples](https://github.com/firebase/functions-samples/tree/master/fcm-notifications)