# Contours

### Introduction
    Contours is a keyboard training program aimed at cochlear implant users with
no prior musical experience. The goal of the software is to establish an
action perception link between learning to play a physical musical instrument
and being able to distinguish between pitches in speech and music.
    To this end, contours teaches users to play musical shapes and logs their
performance data over time. This application was designed specifically for
tablet devices, and a screen size of 9 inches or greater is highly recommended.

### Installation for Research context and further exploration
    A basic form of contours will shortly be made available in a pre-compiled
.apk file immediately useable, with built in ability to train users to play the
built in contour shapes defined in the attached paper. However, to get the full
benefit of customization and modification of the code base, one must download
the github project and the Android-SDK version 15 or higher. We highly recommend
using android studio, as it comes packaged with the development tools
necessary to build and compile the project.

1. clone the contours repository
2. download android studio http://developer.android.com/tools/studio/index.html
3. open android studio and select "import project" from the start menu
4. select the folder of the repository downloaded, and open it
5. enable developer settings on your android device by following these
instructions
http://www.androidcentral.com/android-50-lollipop-basics-how-turn-developer-settings
6. build and run the project by pressing the green play button at the top of
android studio. From this point on the app will be useable on that device, with
any of the modifications you have made, without need to build or run from the
computer again.

### Modifying internal XML data
    The contour shapes are specified as a comma delimited list of note names.
Note names are specified by their note name followed by their octave with C3
being middle C or midi note 60. Modifying or adding to the list of contours as
defined in the xml file arrays.xml, will directly change the contours shapes
presented to the user in the application.

### Server
    An example server written in Python/Flask/MongoDB is available for download
at https://github.com/contoursapp/ContoursServer. It is not required that you
use this server code, but it is fully functional and may  be a good starting
point.
If you host a server that receives data logged from the contours application
over the internet, you must alter the value of the parameter external_server_url
in strings.xml to be the url at which you choose to host the server code.

The data logged by the contours application is sent as a JSON object to the
with the specified server with the following keys representing data logged for
an entire training session:
    total_score,
    difficulty,
    interval_size,
    elapsed_time,
    notes_hit,
    notes_missed,
    longest_streak,
    average_streak,
    date,
    singles,
    survey_responses

singles is a JSON array containing data for user performance with regard to
individual contours shapes. Each JSON object in singles contains the following:
    contourId,
    difficulty,
    noteGap,
    sound,
    contourStartMidiNote,
    completionTime,
    numberOfErrors,
    percentError,
    successDuration,
    interOnsetIntervalStdDev,
    date

Additionally, survey_responses is an array containing the users responses to
Each of the questions asked in the training session.

### Modifying pure data patches
    All of the sound is implemented in pure data, so researchers who wish to
implement their own patches or modify the existing ones may do so. The patches
exist compressed in app/res/raw/synthpatch.zip. Unzipping this file will give
access to the source .pd files. A good place to start would be to modify the
provided fully featured subtractive synth. The requirement in the current
version is that all patches receive midi note data in the following format:
note midiNum velocity ex. [note 60 94] would be a midi on command for midi note
60 at a velocity of 94.
For more information  on the pure data visual programming language see:
https://puredata.info/

### Libraries Used:
    1. kshoji - USB-MIDI-Driver https://github.com/kshoji/USB-MIDI-Driver
    2. google - gson https://github.com/google/gson
    3. greenrobot - eventbus http://greenrobot.org/eventbus/
    4. greenrobot - greenDAO https://github.com/greenrobot/greenDAO
    5. google - volley https://android.googlesource.com/platform/frameworks/volley/
    6. libpd/pdforandroid https://github.com/libpd/pd-for-android
