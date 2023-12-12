let localVideo = document.getElementById("local-video");
let remoteVideo = document.getElementById("remote-video");

localVideo.style.opacity = 0;
remoteVideo.style.opacity = 0;

localVideo.onplaying = () => { localVideo.style.opacity = 1 };
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1 };

let peer;
function init(userId) {
//    console.log("init");

    peer = new Peer(userId, {
            port: 443,
            path: '/'
        });

    peer.on('open', function(id) {
//        console.log("onPeerConnected");
      });

      listen();
}

let localStream;
function listen() {
//    console.log("listen");
    peer.on('call', (call) => {
    navigator.getUserMedia({
            audio: true,
            video: true
        }, (stream) => {
            localVideo.srcObject = stream;
            localStream = stream;

//            console.log("answer call");
            call.answer(stream);
            call.on('stream', (remoteStream) => {
//                console.log("listen settings videos");
                remoteVideo.srcObject = remoteStream;

                remoteVideo.className = "primary-video";
                localVideo.className = "secondary-video";

            })

        })
})
}
function startCall(otherUserId) {
//    console.log("startCall");
//    console.log("id: " + otherUserId);

    navigator.getUserMedia({
        audio: true,
        video: true
    }, (stream) => {

        localVideo.srcObject = stream;
        localStream = stream;

//        console.log("make call");
        const call = peer.call(otherUserId, stream);
        call.on('stream', (remoteStream) => {
            remoteVideo.srcObject = remoteStream;

            remoteVideo.className = "primary-video";
            localVideo.className = "secondary-video";
        })

    })
}

function disconnectCall() {
    peer.on('disconnected', function () {
        console.log("disconnectCall");
    });
    peer.disconnect();
//    peer.close();

//    localVideo.srcObject.getVideoTracks().forEach(track => {
//      track.stop()
//      localVideo.srcObject.removeTrack(track);
//    });
}
//
function toggleVideo(b) {
    if (b == "true") {
        localStream.getVideoTracks()[0].enabled = true
    } else {
        localStream.getVideoTracks()[0].enabled = false
    }
}

function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
}