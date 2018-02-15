 Ready2Meet
 ===================
 More than an event organizer
----

![picture alt](https://github.com/KuechA/mobEvent/blob/master/VFriend-master/app/src/main/res/mipmap-hdpi/ic_launcher.png)

Platform: Android

## Application description ##

Ready2Meet is an Android app which aims at facilitating creating events and inviting people. The core of the application, namely organizing an event, can be used in two different modes:

1. Planned event
2. Spontaneous event

For both modes, the user creates an event (including a start and end time, location, type of event and eventually a name). In the planned event mode, the user can invite specific people (friends) while for the spontaneous event, it is not possible to invite specific people. Instead, everyone will be able to join the event which should only be displayed to a user if he is close to the event's location. Like this, the organizer of the event is able to spontaneously meet with people based on their location.

Once a user joined an event, she/he is able to share pictures of the event in the app and eventually influence the event's outcome e.g. by voting for music on a party. A chatroom is available to communicate with the people before or after the event and thus allow networking and spreading information to all participants.

For all kinds of events, the organizing person can add a list of required material (e.g. drinks or food) so that the guests can register to bring some of the material and thus easing the organization.

Finally, every user invited to an event is provided additional information like the weather forecast for outdoor events and whether he is available on the date when the event is scheduled.

## Features ##

The features of our event organization app include

* Create an event (e.g. party, sports, hiking, lunch, ...)
    * Invite friends - Further development
    * Invite people in a close area. I.e., the user can specify a radius to invite people and the people are notified if they are interested in this kind of events
* Chatroom for event
* Share pictures of the event
* Get additional information for the event
    * Weather forecast for outdoor events
    * Check if timeslot is free in your calendar
* Share the event in other networks e.g. on facebook - Further development
* Enable music organization/voting during the event - Further development
* Who brings what? - Further development

## Frameworks used ##
* Firebase
    * Realtime database
    * Storage
    * Authentication
    * Messaging
* Google play services
    * Maps
    * Places
* OpenWeatherMap API
* UI specific:
    * RoundCornerProgressBar
    * Picasso
    * CircleImageView
    * CustomCheckbox

## Required Permissions ##
* INTERNET
* ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
* READ_CALENDAR, WRITE_CALENDAR
