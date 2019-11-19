/***************************************/
/*    Set up the static file server    */
/*include static file webserver library*/
var static = require('node-static');

/*Include the http server library*/
var http = require('http');

/* Assume running on Heroku */
var port = process.env.PORT;
var directory = __dirname + '/public';

/*If not on Heroku, adjust port and directory info*/
if (typeof (port) == 'undefined' || !port) {
    directory = './public';
    port = 8080;
}

/*Set up static web server. Delivers files from filesystem*/
var file = new static.Server(directory);

/*Construct HTTP server that gets files from fileserver*/
var app = http.createServer(
    function (request, response) {
        request.addListener('end',
            function () {
                file.serve(request, response);
            }
        ).resume();
    }
).listen(port);

console.log('The Server is running');


/************************************/
/*   Set up the web socket server   */

/* Registry of socket_id's and player info */
var players = []

var io = require('socket.io').listen(app)

io.sockets.on('connection', function (socket) {

    log('Client connection by ' + socket.id)

    function log() {
        var array = ['*** Server Log Message: '];
        for (var i = 0; i < arguments.length; i++) {
            array.push(arguments[i]);
            console.log(arguments[i])
        }
        socket.emit('log', array);
        socket.broadcast.emit('log', array);
    }


    /* join room command */
    /* payload:
     *   {
         *      'room': room to join,
     *      'username': username of person joining
     *   }
     * 
     *   join_room_response:
     *   {
     *      'result': 'success',
     *      'room': room joined,
     *      'username': username that joined,
     *      'socket_id': socket id that joined,
     *      'membership': number of people in the room including this joiner 
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure message,
     *   }
     */
    socket.on('join_room', function (payload) {
        log("'join room' command" + JSON.stringify(payload));

        //Check that the client sent a payload
        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'join_room had no payload, command aborted';
            log(error_message);
            socket.emit('join_room_response', { result: 'fail', message: error_message })
            return;
        }

        //Check that the client has a room to join
        var room = payload.room;
        if (('undefined' === typeof room) || (!room)) {
            var error_message = 'join_room didn\'t specify a room, command aborted';
            log(error_message);
            socket.emit('join_room_response', { result: 'fail', message: error_message })
            return;
        }

        //Check that a username has been provided
        var username = payload.username;
        if (('undefined' === typeof username) || (!username)) {
            var error_message = 'join_room didn\'t specify a username, command aborted';
            log(error_message);
            socket.emit('join_room_response', { result: 'fail', message: error_message })
            return;
        }

        //Store info about this new player
        players[socket.id] = {};
        players[socket.id].username = username;
        players[socket.id].room = room;

        //join the room
        socket.join(room);

        //get the room object
        var roomObject = io.sockets.adapter.rooms[room];

        //Annouce the new player's arrival
        var numClients = roomObject.length;
        var success_data = {
            result: 'success',
            room: room,
            username: username,
            socket_id: socket.id,
            membership: numClients
        };
        io.in(room).emit('join_room_response', success_data)

        for (var socket_in_room in roomObject.sockets) {
            var success_data = {
                result: 'success',
                room: room,
                username: players[socket_in_room].username,
                socket_id: socket_in_room,
                membership: numClients
            };

            socket.emit('join_room_response', success_data)
        }

        log('join_room success')

    })


    /* Disconnect command */
    socket.on('disconnect', function (payload) {
        log('Client disconnected ' + JSON.stringify(players[socket.id]));

        if ('undefined' != typeof players[socket.id] && players[socket.id]) {
            var username = players[socket.id].username
            var room = players[socket.id].room
            var payload = {
                username: username,
                socket_id: socket.id
            }

            delete players[socket.id];
            io.in(room).emit('player_disconnected', payload);
        }
    })

    /* send message command */
    /*   payload:
     *   {
     *      'message': message to send,
     *      'username': username of person joining
     *   }
     * 
     *   send_message_response:
     *   {
     *      'result': 'success',
     *      'message': message sent,
     *      'username': username that joined
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure message,
     *   }
     */

    socket.on('send_message', function (payload) {
        log('server recieved a command', 'send_message', payload);

        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'send_message had no payload, command aborted';
            log(error_message);
            socket.emit('send_message_response', { result: 'fail', message: error_message })
            return;
        }

        var room = payload.room;
        if (('undefined' === typeof room) || (!room)) {
            var error_message = 'send_message didn\'t specify a room, command aborted';
            log(error_message);
            socket.emit('send_message_response', { result: 'fail', message: error_message })
            return;
        }

        var username = players[socket.id].username;
        if (('undefined' === typeof username) || (!username)) {
            var error_message = 'send_message didn\'t specify a username, command aborted';
            log(error_message);
            socket.emit('send_message_response', { result: 'fail', message: error_message })
            return;
        }

        var message = payload.message;
        if (('undefined' === typeof message) || (!message)) {
            var error_message = 'send_message didn\'t specify a username, command aborted';
            log(error_message);
            socket.emit('send_message_response', { result: 'fail', message: error_message })
            return;
        }

        var success_data = {
            result: 'success',
            room: room,
            username: username,
            message: message,
        }

        io.in(room).emit('send_message_response', success_data);
        log('Message sent to room ' + room + ' by ' + username);
    })


    /* invite command */
    /*   payload:
     *   {
     *      'requested_user': the socket id of the person to be invited
     *   }
     * 
     *   invite_response:
     *   {
     *      'result': 'success',
     *      'socket_id': the socket id of the person being invited
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure invite,
     *   }
     * 
     *   invited:
     *   {
     *      'result': 'success',
     *      'socket_id': the socket id of the person being invited
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure invite,
     *   }
     * */
    socket.on('invite', function (payload) {
        log('invite with'+ JSON.stringify(payload));

        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'invite had no payload, command aborted';
            log(error_message);
            socket.emit('invite_response', { result: 'fail', message: error_message })
            return;
        }

        var requested_user = payload.requested_user;
        if (('undefined' === typeof requested_user) || (!requested_user)) {
            var error_message = 'invite didn\'t specify a username to invite, command aborted';
            log(error_message);
            socket.emit('invite_response', { result: 'fail', message: error_message })
            return;
        }

        var room = players[socket.id].room;
        var roomObj = io.sockets.adapter.rooms[room];
        /* make sure invited user is in the room */
        if(!roomObj.sockets.hasOwnProperty(requested_user)){
            var error_message = 'invite requested a user that wasn\'t in the room';
            log(error_message);
            socket.emit('invite_response', { result: 'fail', message: error_message })
            return;
        }

        /* respond with successful data */
        var success_data = {
            result: 'success',
            socket_id: requested_user
        }

        socket.emit('invite_response', success_data);

        var success_data = {
            result: 'success',
            socket_id: socket.id
        }

        socket.to(requested_user).emit('invited', success_data)

        log('invited successful')
    })

    /* uninvite command */
    /*   payload:
     *   {
     *      'requested_user': the socket id of the person to be uninvited
     *   }
     * 
     *   uninvite_response:
     *   {
     *      'result': 'success',
     *      'socket_id': the socket id of the person being uninvited
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure uninvite,
     *   }
     * 
     *   uninvited:
     *   {
     *      'result': 'success',
     *      'socket_id': the socket id of the person doing the uninviting
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure uninvited,
     *   }
     * */
    socket.on('uninvite', function (payload) {
        log('uninvite with'+ JSON.stringify(payload));

        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'uninvite had no payload, command aborted';
            log(error_message);
            socket.emit('uninvite_response', { result: 'fail', message: error_message })
            return;
        }

        var username = players[socket.id].username;
        if (('undefined' === typeof username) || (!username)) {
            var error_message = 'uninvite couldn\'t identify who sent the uninvite, command aborted';
            log(error_message);
            socket.emit('uninvite_response', { result: 'fail', message: error_message })
            return;
        }

        var requested_user = payload.requested_user;
        if (('undefined' === typeof requested_user) || (!requested_user)) {
            var error_message = 'uninvite didn\'t specify a username to uninvite, command aborted';
            log(error_message);
            socket.emit('uninvite_response', { result: 'fail', message: error_message })
            return;
        }

        var room = players[socket.id].room;
        var roomObj = io.sockets.adapter.rooms[room];
        /* make sure invited user is in the room */
        if(!roomObj.sockets.hasOwnProperty(requested_user)){
            var error_message = 'invite requested a user that wasn\'t in the room';
            log(error_message);
            socket.emit('uninvite_response', { result: 'fail', message: error_message })
            return;
        }

        /* respond with successful data */
        var success_data = {
            result: 'success',
            socket_id: requested_user
        }

        socket.emit('uninvite_response', success_data);

        var success_data = {
            result: 'success',
            socket_id: socket.id
        }

        socket.to(requested_user).emit('uninvited', success_data)

        log('uninvite successful')
    })

    /* game_start command */
    /*   payload:
     *   {
     *      'requested_user': the socket id of the person to be invited
     *   }
     * 
     *   invite_response:
     *   {
     *      'result': 'success',
     *      'socket_id': the socket id of the person you're playing with
     *      'game_id': ID of the game session
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure invite,
     *   }
     * 
     *   invited:
     *   {
     *      'result': 'success',
     *      'socket_id': the socket id of the person being invited
     *   }
     *   or
     *   {
     *      'result': 'fail',
     *      'message': failure invite,
     *   }
     * */
    socket.on('game_start', function (payload) {
        log('game_start '+ JSON.stringify(payload));

        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'game_start had no payload, command aborted';
            log(error_message);
            socket.emit('game_start_reponse', { result: 'fail', message: error_message })
            return;
        }

        var username = players[socket.id].username;
        if (('undefined' === typeof username) || (!username)) {
            var error_message = 'game_start couldn\'t identify who sent the invite, command aborted';
            log(error_message);
            socket.emit('invite_response', { result: 'fail', message: error_message })
            return;
        }

        var requested_user = payload.requested_user;
        if (('undefined' === typeof requested_user) || (!requested_user)) {
            var error_message = 'game_start didn\'t specify a username to invite, command aborted';
            log(error_message);
            socket.emit('invite_response', { result: 'fail', message: error_message })
            return;
        }

        var room = players[socket.id].room;
        var roomObj = io.sockets.adapter.rooms[room];
        /* make sure playing user is in the room */
        if(!roomObj.sockets.hasOwnProperty(requested_user)){
            var error_message = 'game_start requested a user that wasn\'t in the room';
            log(error_message);
            socket.emit('invite_response', { result: 'fail', message: error_message })
            return;
        }

        /* respond with successful data */
        var game_id = Math.floor(1+Math.random()* 0x10000).toString(16).substring(1)
        var success_data = {
            result: 'success',
            socket_id: requested_user,
            game_id: game_id
        }

        socket.emit('game_start_response', success_data);

        var success_data = {
            result: 'success',
            socket_id: socket.id,
            game_id: game_id
        }

        socket.to(requested_user).emit('game_start_response', success_data)

        log('game_start successful')
    })
})