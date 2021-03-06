/* functions for general use */

//Get the URL value desired
function getUrlParams(whichParam) {
    var pageURL = window.location.search.substring(1);
    var pageURLVars = pageURL.split("&");

    for (var i = 0; i < pageURLVars.length; i++) {
        var paramName = pageURLVars[i].split("=");
        if (paramName[0] == whichParam) {
            return paramName[1];
        }
    }
}

//set username
var username = getUrlParams("username");
if ("undefined" == typeof username || !username) {
    username = "Anonymous_" + Math.random() * 10;
} else {
    username = username.split("%20").join("_");
}

//set char room
var chat_room = getUrlParams("game_id");
if ("undefined" == typeof chat_room || !chat_room) {
    chat_room = "lobby"
}

/* Connect to the socket server */
var socket = io.connect();

/* Log Message Actions */
socket.on("log", function (array) {
    console.log.apply(console, array);
});




/***** LISTENER FUNCTIONS *****/

/* Join Room Actions */
socket.on("join_room_response", function (payload) {
    console.log("join response: " + payload.result);
    if (payload.result == "fail") {

        if (!('undefined' === typeof payload.username) || (payload.username)) {
            $('div.container-fluid').hide();

            $('.alerts').append('<div class="alert alert-dismissible alert-danger text-center">'
                + '<button type="button" class="close" data-dismiss="alert">&times;</button> '
                + '<strong>Dang it! </strong>Looks like "' + payload.username + '" is being used by someone else. '
                + 'Please <a href="name.html" style="text-decoration:underline;">try another username!</a>'
                + '</div>');

            setTimeout(function () { window.location.replace('name.html') }, 10000)

        } else {
            alert(payload.message);
        }
        return;
    }

    //Handle new player message
    if (payload.socket_id == socket.id) {
        return;
    }

    /* Add new lobby table row for new player */
    var dom_elements = $(".socket_" + payload.socket_id);

    if (dom_elements.length == 0) {
        //For new entries

        //Add the socket
        var nodeA = $("<div></div>")
        nodeA.addClass("socket_" + payload.socket_id);
        nodeA.addClass("w-100")

        //Add the username
        var nodeB = $("<div></div>")
        nodeB.addClass("socket_" + payload.socket_id);
        nodeB.addClass("col-8 text-right")
        nodeB.append("<h4>" + payload.username + "</h4>")

        //Add the invite button
        var nodeC = $("<div></div>")
        nodeC.addClass("socket_" + payload.socket_id);
        nodeC.addClass("col-2 text-left")
        var buttonC = makeInviteButton(payload.socket_id);
        nodeC.append(buttonC)

        // Create kill button placeholder
        var nodeK = $("<div><div>")
        nodeK.addClass("socket_" + payload.socket_id)
        nodeK.addClass("bot col-2 text-left")

        //Add kill button to bots
        if (payload.is_bot) {
            var buttonK = makeKillButton(payload.socket_id)
            nodeK.append(buttonK)
        }

        //Add all parts to the main node
        let mainNode = $('<li></li>')
        mainNode.addClass("card-header d-flex align-items-center py-1")
        mainNode.addClass("socket_" + payload.socket_id)
        mainNode.append(nodeA, nodeB, nodeC, nodeK)
        mainNode.hide();

        $('#players').append(mainNode)
        mainNode.slideDown(1000);
    } else {
        //For existing entries, just create the invite button
        uninvite(payload.socket_id)
        var buttonC = makeInviteButton(payload.socket_id);
        $(".socket_" + payload.socket_id + " button").replaceWith(buttonC)

        //Add kill button to bots
        if (payload.is_bot) {
            var nodeK = $("<div><div>")
            nodeK.addClass("socket_" + payload.socket_id)
            nodeK.addClass("bot col-2 text-left")
            var buttonK = makeKillButton(payload.socket_id)
            nodeK.append(buttonK)
            nodeK.hide();
            $('#players').append(nodeK)
            nodeK.slideDown(1000);
        }

        dom_elements.slideDown(1000);
    }

    //Message for new player joining
    var newHTML = "<p><b>" + payload.username + "</b> has joined</p>";
    var newNode = $(newHTML);
    newNode.hide();
    $("#messages").prepend(newNode);
    $('#filler-player').slideUp(200, function () { $(this).remove(); })
    newNode.slideDown(1000);

});

/* Leave Room Actions */
socket.on("player_disconnected", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    //If we're being told we're leaving, ignore message
    if (payload.socket_id == socket.id) {
        return;
    }

    /* Animate out all the leaving player's elements */
    $("li.socket_" + payload.socket_id).removeClass("py-1").addClass("py-0")

    var dom_elements = $(".socket_" + payload.socket_id);
    if (dom_elements.length != 0) {
        let time = 1000
        dom_elements.slideUp(time, function () { $(this).remove(); addFillerText(); })
    }

    //Message to show player leaving
    var newHTML = "<p><b>" + payload.username + "</b> left</p>";
    var newNode = $(newHTML);
    newNode.hide();
    $("#messages").prepend(newNode);
    newNode.slideDown(1000);

    //Adds filler text if no players available
    function addFillerText() {
        if (!$.trim($('#players').html()).length) {
            let fillerText = "No one's here :/"
            let mainNode = $('<li></li>')
            mainNode.addClass("card-header align-items-center pt-2 pb-0")
            mainNode.attr("id", "filler-player")

            let divNode = $("<div></div>")
            divNode.addClass("col text-center")

            let textNode = $("<h4>" + fillerText + "</h4>")
            textNode.addClass("text-center")
            textNode.css({ "width": "max-content", "margin-left": "auto", "margin-right": "auto" });

            divNode.append(textNode)
            mainNode.append(divNode)
            mainNode.hide();

            $('#players').append(mainNode)
            mainNode.slideDown(1000);
        }
    }
});

/* Send a new message */
function send_message() {
    var payload = {};
    payload.room = chat_room;
    payload.username = username;
    payload.message = $("#send_message_holder").val();
    console.log(
        "*** Client Log Message: 'send_message' payload: " + JSON.stringify(payload)
    );
    socket.emit("send_message", payload);

    //blank the message holder after the message is posted
    $("input[id=send_message_holder]").val("");
}

//Handle New Message
socket.on("send_message_response", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    var newHTML = "<p><b>" + payload.username + " says:</b> " + payload.message + "</p>"
    var newNode = $(newHTML)
    newNode.hide()

    $("#messages").prepend(newNode);
    newNode.slideDown(1000)
});


/* Invitations */

//Handle send invite
socket.on("invite_response", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    var newNode = makeUninviteButton(payload.socket_id);
    $('.socket_' + payload.socket_id + ' button.invite').replaceWith(newNode)
})

/* Invite a player to a game */
function invite(who) {
    var payload = {};
    payload.requested_user = who;

    console.log("*** Client Log Message: 'invite' payload: " + JSON.stringify(payload));
    socket.emit('invite', payload)
}

//Notify that we have been invited
socket.on("invited", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    var newNode = makePlayButton(payload.socket_id);
    $('.socket_' + payload.socket_id + ' button.invite').replaceWith(newNode)
})


/* Uninvitations */

//Handle uninvite response
socket.on("uninvite_response", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    var newNode = makeInviteButton(payload.socket_id);
    $('.socket_' + payload.socket_id + ' button.uninvite').replaceWith(newNode)
})

/* Uninvite a player from a game */
function uninvite(who) {
    var payload = {};
    payload.requested_user = who;

    console.log("*** Client Log Message: 'uninvite' payload: " + JSON.stringify(payload));
    socket.emit('uninvite', payload)
}

//Notify that we have been uninvited
socket.on("uninvited", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    var newNode = makeInviteButton(payload.socket_id);
    $('.socket_' + payload.socket_id + ' button.play').replaceWith(newNode)
})

/* Accepting Invites */

//Handle game_start response
socket.on("game_start_response", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }

    var newNode = makePlayButton(payload.socket_id);
    $('.socket_' + payload.socket_id + ' button.uninvite').replaceWith(newNode)
    $('.socket_' + payload.socket_id + ' button.play').replaceWith(newNode)

    window.location.href = "game.html?username=" + username + "&game_id=" + payload.game_id;

})

/* Notification we have had a game begin */
function game_start(who) {
    var payload = {};
    payload.requested_user = who;

    console.log("*** Client Log Message: 'game_start' payload: " + JSON.stringify(payload));
    socket.emit('game_start', payload)
}


function spawn_bot(difficulty, ai_type) {
    var payload = {};
    payload.difficulty = difficulty
    payload.ai_type = ai_type
    payload.role = "play"
    payload.username = username + "_bot_" + ai_type + "_" + difficulty
    payload.opponent = "none";

    console.log("*** Client Log Message: 'spawn_bot' payload: " + JSON.stringify(payload));
    socket.emit('spawn_bot', payload)
}

function spawn_bots(difficulty1, ai_type1, difficulty2, ai_type2) {
    var payload = {};

    username1 = username + "_" + ai_type1 + "_" + difficulty1 + Math.floor(Math.random()*100)
    username2 = username + "_" + ai_type2 + "_" + difficulty2 + Math.floor(Math.random()*100)

    payload.username = username1
    payload.difficulty = difficulty1
    payload.ai_type = ai_type1
    payload.role = 'play'
    payload.opponent = username2
    console.log("*** Client Log Message: 'spawn_bot' payload: " + JSON.stringify(payload));
    socket.emit('spawn_bot', payload)

    payload.username = username2
    payload.difficulty = difficulty2
    payload.ai_type = ai_type2
    payload.role = 'invite'
    payload.opponent = username1
    console.log("*** Client Log Message: 'spawn_bot' payload: " + JSON.stringify(payload));
    socket.emit('spawn_bot', payload)
}

socket.on("spawn_bot_response", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }
})

function kill_bot(socket_id) {
    var payload = {};
    payload.to_kill = socket_id
    payload.terminator = username

    console.log("*** Client Log Message: 'kill_bot' payload: " + JSON.stringify(payload));
    socket.emit('kill_bot', payload)
}

socket.on("kill_bot_response", function (payload) {
    if (payload.result == "fail") {
        alert(payload.message);
        return;
    }
})

/*** HTML FUNCTIONS ***/

/* Create an Invite Button */
function makeInviteButton(socket_id) {
    var newHTML = "<button type='button' class='btn btn-outline-primary invite'>Invite</button>"
    var newNode = $(newHTML)
    newNode.click(function () {
        invite(socket_id)
    })
    return (newNode)
}

/* Create an Uninvite Button */
function makeUninviteButton(socket_id) {
    var newHTML = "<button type='button' class='btn btn-info uninvite'>Uninvite</button>"
    var newNode = $(newHTML)
    newNode.click(function () {
        uninvite(socket_id)
    })
    return (newNode)
}

/* Create a Play Button */
function makePlayButton(socket_id) {
    var newHTML = "<button type='button' class='btn btn-success play'>Play</button>"
    var newNode = $(newHTML)
    newNode.click(function () {
        game_start(socket_id)
    })
    return (newNode)
}

/* Create a Kill Button */
function makeKillButton(socket_id) {
    var newHTML = "<button type='button' class='btn btn-danger kill'>Kill</button>"
    var newNode = $(newHTML)
    newNode.click(function () {
        kill_bot(socket_id)
    })
    return (newNode)
}

function goHome() {
    window.location.href = 'index.html'
}

/*** Ready Load ***/
$(function () {

    // Announce entry
    var payload = {};
    payload.room = chat_room;
    payload.username = username;
    payload.is_bot = false;

    console.log("*** Client Log Message: 'join_room' payload: " + JSON.stringify(payload));
    socket.emit("join_room", payload);

    $('#quit').append('<a href="lobby.html?username=' + username + '" class="btn btn-danger" role="button" aria-pressed="true">Quit</a>')

    // Listen for bot spawners
    $("button#spawn-single").click(function () {
        var difficulty = $("select#difficulty").children("option:selected").val()
        var type = $("select#type").children("option:selected").val()

        spawn_bot(difficulty, type)
    });

    $("button#spawn-two").click(function () {
        var difficulty1 = $("select#difficulty1").children("option:selected").val()
        var type1 = $("select#type1").children("option:selected").val()

        var difficulty2 = $("select#difficulty2").children("option:selected").val()
        var type2 = $("select#type2").children("option:selected").val()

        spawn_bots(difficulty1, type1, difficulty2, type2)
    });

});


var old_board = [
    ["?", "?", "?", "?", "?", "?", "?", "?"],
    ["?", "?", "?", "?", "?", "?", "?", "?"],
    ["?", "?", "?", "?", "?", "?", "?", "?"],
    ["?", "?", "?", "?", "?", "?", "?", "?"],
    ["?", "?", "?", "?", "?", "?", "?", "?"],
    ["?", "?", "?", "?", "?", "?", "?", "?"],
    ["?", "?", "?", "?", "?", "?", "?", "?"],
    ["?", "?", "?", "?", "?", "?", "?", "?"],
]

var my_color = " ";
var interval_timer;
socket.on('game_update', function (payload) {


    console.log("*** Client Log Message: 'game_update' payload: " + JSON.stringify(payload));
    /* Check for good board update */
    if (payload.result == "fail") {
        console.log(payload.message)
        window.location.href = 'lobby.html?username=' + username
        return;
    }

    /* Check for a good board in the payload */
    var board = payload.game.board;
    if (('undefined' === typeof board) || !board) {
        console.log("Internal error: received malformed board update from the server")
        return;
    }

    /* Update my color */
    var border;
    if (socket.id == payload.game.player_white.socket) {
        my_color = "white"
        $('link[rel="icon"]').attr('href', 'assets/images/white_tile.png')
    } else if (socket.id == payload.game.player_black.socket) {
        my_color = "black"
        $('link[rel="icon"]').attr('href', 'assets/images/black_tile.png')
    } else {
        /* Something weird happened, send client back to lobby */
        console.log('Something weird happened, sending back to lobby')
        window.location.href = "lobby.html?username=" + username
    }

    if (my_color == 'black') {
        $('#game_label').html('<h3 id="game_label">Game with ' + payload.game.player_white.username)
    } else {
        $('#game_label').html('<h3 id="game_label">Game with ' + payload.game.player_black.username)
    }

    $('#my_color').html('<h3 id="my_color" style="color: ' + my_color + ';">You\'re playing as ' + capFirst(my_color) + '</h3>')
    if (my_color == payload.game.whose_turn) {
        $('#timer').html('<h4>Your turn! Elapsed time: <span id=\"elapsed\"></span></h4>')
    } else {
        $('#timer').html('<h4>Waiting on ' + payload.game.whose_turn + ': <span id=\"elapsed\"></span></h4>')
    }

    clearInterval(interval_timer);
    interval_timer = setInterval(function (last_time) {
        return function () {
            var d = new Date();
            var elapsedmilli = d.getTime() - last_time;
            var minutes = Math.floor(elapsedmilli / (60 * 1000))
            var seconds = Math.floor((elapsedmilli % (60 * 1000)) / 1000)

            if (seconds < 10) {
                $('#elapsed').html(minutes + ':0' + seconds)
            } else {
                $('#elapsed').html(minutes + ':' + seconds)
            }
        }
    }(payload.game.last_move_time),
        1000)

    blacksum = 0;
    whitesum = 0;

    /* Animate changes to the board */
    var row, col;
    for (row = 0; row < 8; row++) {
        for (col = 0; col < 8; col++) {

            //tally score
            if (board[row][col] == 'b') {
                blacksum++
            } else if (board[row][col] == 'w') {
                whitesum++
            }

            /* if a board space has changed */
            if (old_board[row][col] != board[row][col]) {
                if (old_board[row][col] == "?" && board[row][col] == " ") {
                    $('#' + row + '_' + col).addClass('bg-success')
                }
                else if (old_board[row][col] == "?" && board[row][col] == "w") {
                    $('#' + row + '_' + col).html('<img src="assets/images/empty_to_white.gif" alt="white square"/>')
                }
                else if (old_board[row][col] == "?" && board[row][col] == "b") {
                    $('#' + row + '_' + col).html('<img src="assets/images/empty_to_black.gif" alt="black square"/>')
                }
                else if (old_board[row][col] == " " && board[row][col] == "w") {
                    $('#' + row + '_' + col).html('<img src="assets/images/empty_to_white.gif" alt="white square"/>')
                }
                else if (old_board[row][col] == " " && board[row][col] == "b") {
                    $('#' + row + '_' + col).html('<img src="assets/images/empty_to_black.gif" alt="black square"/>')
                }
                else if (old_board[row][col] == "w" && board[row][col] == " ") {
                    $('#' + row + '_' + col).html('<img src="assets/images/white_to_empty.gif" alt="empty square"/>')
                }
                else if (old_board[row][col] == "b" && board[row][col] == " ") {
                    $('#' + row + '_' + col).html('<img src="assets/images/black_to_empty.gif" alt="empty square"/>')
                }
                else if (old_board[row][col] == "w" && board[row][col] == "b") {
                    $('#' + row + '_' + col).html('<img src="assets/images/white_to_black.gif" alt="black square"/>')
                }
                else if (old_board[row][col] == "b" && board[row][col] == "w") {
                    $('#' + row + '_' + col).html('<img src="assets/images/black_to_white.gif" alt="white square"/>')
                } else {
                    $('#' + row + '_' + col).html('<img src="assets/images/error.gif" alt="error square"/>')
                }
            }
            /* Interactivity */
            $('#' + row + '_' + col).off('click');
            $('#' + row + '_' + col).removeClass('valid_move')

            if (payload.game.whose_turn === my_color) {
                if (payload.game.legal_moves[row][col] === my_color.substr(0, 1)) {
                    $('#' + row + '_' + col).addClass('valid_move')
                    $('#' + row + '_' + col).click(function (r, c) {
                        return function () {
                            var payload = {}
                            payload.row = r
                            payload.column = c
                            payload.color = my_color;
                            console.log('*** Client log message: "play_token" payload: ' + JSON.stringify(payload))
                            socket.emit('play_token', payload)
                        };
                    }(row, col))
                }
            }
        }
    }

    let total = (blacksum + whitesum)

    $('#blacksum').html('Black: ' + blacksum)
    let bperc = blacksum / total * 100
    $('#blacksum-bar').attr('style', 'width: ' + bperc + '%;')

    $('#whitesum').html('White: ' + whitesum)
    let wperc = whitesum / total * 100
    $('#whitesum-bar').attr('style', 'width: ' + wperc + '%;')


    old_board = board;
})

socket.on('play_token_response', function (payload) {
    console.log("*** Client Log Message: 'play_token_response' payload: " + JSON.stringify(payload));
    /* Check for good play_token response */
    if (payload.result == "fail") {
        console.log(payload.message)
        return;
    }
})

socket.on('game_over', function (payload) {
    console.log("*** Client Log Message: 'game_over' payload: " + JSON.stringify(payload));
    /* Check for good play_token response */
    if (payload.result == "fail") {
        console.log(payload.message)
        return;
    }

    /* Show winner and provide link to lobby */
    $('#timer').attr('hidden', true)
    $('#game_over').html('<h1>Game Over</h1><h2>Congratulations ' + payload.winner + '!!</h2>')
    $('#game_over').append('<a href="lobby.html?username=' + username + '" class="btn btn-success btn-lg" role="button" aria-pressed="true">Return to lobby</a>')

})

function capFirst(string) {
    return string.charAt(0).toUpperCase() + string.slice(1)
}