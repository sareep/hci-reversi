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
if (typeof port == 'undefined' || !port) {
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
var playerNames = new Set;
var games = [];

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
        log("'join room' command " + JSON.stringify(payload));

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

        //Check that payload declares bot/human status
        var is_bot = payload.is_bot;
        if ('undefined' === typeof is_bot) {
            var error_message = 'join_room didn\'t specify a bot/human status, command aborted';
            log(error_message);
            socket.emit('join_room_response', { result: 'fail', message: error_message })
            return;
        }

        //Check that the username is not already in use
        if (playerNames.has(username)) {
            var error_message = 'join_room username already taken'
            log(error_message)
            socket.emit('join_room_response', { result: 'fail', message: error_message, username: username })
            return
        }


        //Store info about this new player
        playerNames.add(username)
        players[socket.id] = {};
        players[socket.id].username = username;
        players[socket.id].room = room;
        players[socket.id].is_bot = is_bot;

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
            is_bot: is_bot,
            membership: numClients
        };
        io.in(room).emit('join_room_response', success_data)

        for (var socket_in_room in roomObject.sockets) {
            if (username != players[socket_in_room].username) {
                var success_data = {
                    result: 'success',
                    room: room,
                    username: players[socket_in_room].username,
                    socket_id: socket_in_room,
                    is_bot: players[socket_in_room].is_bot,
                    membership: numClients
                };

                socket.emit('join_room_response', success_data)
            }
        }

        log('join_room success')

        if (room !== 'lobby') {
            send_game_update(socket, room, 'initial update')
        }

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

            playerNames.delete(username)
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
        log('invite with ' + JSON.stringify(payload));

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
        if (!roomObj.sockets.hasOwnProperty(requested_user)) {
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
        log('uninvite with ' + JSON.stringify(payload));

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
        if (!roomObj.sockets.hasOwnProperty(requested_user)) {
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
        log('game_start ' + JSON.stringify(payload));

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
            socket.emit('game_start_reponse', { result: 'fail', message: error_message })
            return;
        }

        var requested_user = payload.requested_user;
        if (('undefined' === typeof requested_user) || (!requested_user)) {
            var error_message = 'game_start didn\'t specify a username to invite, command aborted';
            log(error_message);
            socket.emit('game_start_reponse', { result: 'fail', message: error_message })
            return;
        }

        var room = players[socket.id].room;
        var roomObj = io.sockets.adapter.rooms[room];
        /* make sure playing user is in the room */
        if (!roomObj.sockets.hasOwnProperty(requested_user)) {
            var error_message = 'game_start requested a user that wasn\'t in the room';
            log(error_message);
            socket.emit('game_start_reponse', { result: 'fail', message: error_message })
            return;
        }

        /* respond with successful data */
        var game_id = Math.floor(1 + Math.random() * 0x10000).toString(16).substring(1)
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

    /* play_token command */
    /*   payload:
     *   {
     *      'row': 0-7,
     *      'column': 0-7,
     *      'color': 'white' or 'black'
     *   }
     * 
     *   if success, success message followed by game_update message
     *   play_token_response:
     *   {
     *      'result' : 'success'
     *   }
     *   or
     *   {
     *      'result' : 'fail',
     *      'message' : failure message
     *   }
     * */
    socket.on('play_token', function (payload) {
        log('play_token with ' + JSON.stringify(payload));

        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'play_token had no payload, command aborted';
            log(error_message);
            socket.emit('play_token_reponse', { result: 'fail', message: error_message })
            return;
        }

        var player = players[socket.id];
        if (('undefined' === typeof player) || (!player)) {
            var error_message = 'server doesn\'t recognize you. try going back one screen.';
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        var username = players[socket.id].username;
        if (('undefined' === typeof username) || (!username)) {
            var error_message = 'play_token cannot identify who sent the message.';
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        var game_id = players[socket.id].room;
        if (('undefined' === typeof game_id) || (!game_id)) {
            var error_message = 'play_token cannot find your game board'
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        var row = payload.row;
        if (('undefined' === typeof row) || row < 0 || row > 7) {
            var error_message = 'play_token did not specify a valid row, command aborted'
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        var column = payload.column;
        if (('undefined' === typeof column) || column < 0 || column > 7) {
            var error_message = 'play_token did not specify a valid column, command aborted'
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        var color = payload.color;
        if (('undefined' === typeof color) || (color != 'white' && color != 'black')) {
            var error_message = 'play_token did not specify a valid color, command aborted'
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        var game = games[game_id];
        if (('undefined' === typeof game) || !game) {
            var error_message = 'play_token could not find your game, command aborted'
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        if (color !== game.whose_turn) {
            var error_message = 'play_token message played out of turn'
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        /* If the wrong socket is playing the color */
        if (("white" == game.whose_turn && game.player_white.socket != socket.id) ||
            ("black" == game.whose_turn && game.player_black.socket != socket.id)) {
            var error_message = 'play_token player played wrong color'
            log(error_message);
            socket.emit('play_token_response', { result: 'fail', message: error_message })
            return;
        }

        var success_data = {
            result: 'success'
        }

        socket.emit('play_token_response', success_data)

        /* Change game board */
        if (color == 'white') {
            game.board[row][column] = 'w'
            flip_board('w', row, column, game.board)
            game.whose_turn = 'black'
            game.legal_moves = calculate_valid_moves('b', game.board)
        } else if (color == 'black') {
            game.board[row][column] = 'b'
            flip_board('b', row, column, game.board)
            game.whose_turn = 'white'
            game.legal_moves = calculate_valid_moves('w', game.board)
        }

        var d = new Date();
        game.last_move_time = d.getTime();

        send_game_update(socket, game_id, 'played a token')
    })


    socket.on('spawn_bot', function (payload) {


        /** Screen the payload **/
        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'spawn_bot had no payload, command aborted';
            log(error_message);
            socket.emit('spawn_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        if (('undefined' === typeof payload.username) || (!payload.username)) {
            var error_message = 'spawn_bot had no username, command aborted';
            log(error_message);
            socket.emit('spawn_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        if (('undefined' === typeof payload.ai_type) || (!payload.ai_type)) {
            var error_message = 'spawn_bot had no ai_type, command aborted';
            log(error_message);
            socket.emit('spawn_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        if (('undefined' === typeof payload.difficulty) || (!payload.difficulty)) {
            var error_message = 'spawn_bot had no difficulty, command aborted';
            log(error_message);
            socket.emit('spawn_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        if (('undefined' === typeof payload.train_method) || (!payload.train_method)) {
            var error_message = 'spawn_bot had no train_method, command aborted';
            log(error_message);
            socket.emit('spawn_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        payload.port = port;
        spawn_bot(payload)

        /** Spawn RL Bot(s) **/
        // switch (payload.train_method) {
        //     case "none":
        //         spawn_bot(port, "rl", payload.name, payload.difficulty)
        //         break;

        //     case "rl_bot":
        //         spawn_bot(port, "rl", payload.name, payload.difficulty)
        //         spawn_bot(port, "rl", payload.name + "_trainer", payload.difficulty, "train")
        //         break;

        //     case "ab_bot":
        //         spawn_bot(port, "ab", payload.name, payload.difficulty)
        //         break;

        //     default:
        //         break;
        // }

    })

    /**
     * Creates a bot by finding & running a jar
     * @param {string} port 
     * @param {string} type 
     * @param {string} difficulty 
     * @param {string} name 
     * @param {string} role 
     */
    function spawn_bot(payload) {
        // function spawn_bot(port, difficulty, name, type, role = "play") {
        // let args = payload
        // let jar;

        //get the right jar
        // if (type === "ab") {
        //     // jar = "AB_Bot.jar"
        // } else {
        //     // args.push(role)
        // }

        //spawn the bot
        // TODO describe payload here
        let child = require('child_process').spawn(
            'java', ['-jar', 'bot_exes/Reversi_Bot.jar', JSON.stringify(payload)]
        );


        //handle I/O
        child.stdout.on('data', function (data) {
            console.log(data.toString());
        });

        child.stderr.on('data', function (data) {
            console.log(data.toString());
        });
    }




    socket.on('kill_bot', function (payload) {
        /** Screen the payload **/
        if (('undefined' === typeof payload) || (!payload)) {
            var error_message = 'kill_bot had no payload, command aborted';
            log(error_message);
            socket.emit('kill_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        if (('undefined' === typeof payload.to_kill) || (!payload.to_kill)) {
            var error_message = 'kill_bot had no socket, command aborted';
            log(error_message);
            socket.emit('kill_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        if (('undefined' === typeof payload.terminator) || (!payload.terminator)) {
            var error_message = 'kill_bot had no socket, command aborted';
            log(error_message);
            socket.emit('kill_bot_reponse', { result: 'fail', message: error_message })
            return;
        }

        /** Kill The Bot**/
        payload_out = {}
        payload_out.terminator = payload.terminator
        socket.to(payload.to_kill).emit('terminate', payload_out)
    })


    /***************************************************/
    /* Code for the actual game                        */


    function check_line_match(who, dr, dc, r, c, board) {
        if (board[r][c] === who) {
            return true
        } else if ((r + dr < 0) || (r + dr > 7) || (c + dc < 0) || (c + dc > 7) || board[r][c] === " ") {
            return false;
        } else {
            return check_line_match(who, dr, dc, r + dr, c + dc, board);
        }
    }

    /* Check if board[r][c] contains opposite of 'who' and if opposite end of line (dr->r, dc->c) is 'who' */
    function valid_move(who, dr, dc, r, c, board) {
        var other;
        switch (who) {
            case 'b':
                other = 'w'
                break
            case 'w':
                other = 'b'
                break
            default:
                log('Color problem: ' + who);
                return false;
        }

        if ((r + dr < 0) || (r + dr > 7)
            || (r + dr + dr < 0) || (r + dr + dr > 7)
            || (c + dc < 0) || (c + dc > 7)
            || (c + dc + dc < 0) || (c + dc + dc > 7)
            || (board[r + dr][c + dc] != other)) {
            return false;
        }

        return check_line_match(who, dr, dc, r + dr + dr, c + dc + dc, board)
    }

    function calculate_valid_moves(who, board) {
        let valid_board = [
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "]
        ]


        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                if (board[row][col] === " ") {
                    let nw = valid_move(who, -1, -1, row, col, board);
                    let nn = valid_move(who, -1, 0, row, col, board);
                    let ne = valid_move(who, -1, 1, row, col, board);

                    let ww = valid_move(who, 0, -1, row, col, board);
                    let ee = valid_move(who, 0, 1, row, col, board);

                    let sw = valid_move(who, 1, -1, row, col, board);
                    let ss = valid_move(who, 1, 0, row, col, board);
                    let se = valid_move(who, 1, 1, row, col, board);
                    if (nw || nn || ne || ww || ee || sw || ss || se) {
                        valid_board[row][col] = who;
                    }
                }
            }
        }

        return valid_board
    }

    function create_new_game() {
        var new_game = {};
        new_game.player_white = {}
        new_game.player_black = {}
        new_game.player_white.socket = ''
        new_game.player_white.username = ''
        new_game.player_black.socket = ''
        new_game.player_black.username = ''

        var d = new Date();
        new_game.last_move_time = d.getTime()

        new_game.whose_turn = 'black';

        new_game.board = [
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", "w", "b", " ", " ", " "],
            [" ", " ", " ", "b", "w", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "],
            [" ", " ", " ", " ", " ", " ", " ", " "]
        ]

        new_game.legal_moves = calculate_valid_moves('b', new_game.board);

        return new_game;
    }

    function flip_line(who, dr, dc, r, c, board) {

        if ((r + dr < 0) || (r + dr > 7)
            || (c + dc < 0) || (c + dc > 7)
            || (board[r + dr][c + dc] === ' ')) {
            return false;
        }

        if (board[r + dr][c + dc] === who) {
            return true;
        } else if (flip_line(who, dr, dc, r + dr, c + dc, board)) {
            board[r + dr][c + dc] = who
            return true
        } else {
            return false
        }

    }

    function flip_board(who, row, col, board) {

        flip_line(who, -1, -1, row, col, board);
        flip_line(who, -1, 0, row, col, board);
        flip_line(who, -1, 1, row, col, board);

        flip_line(who, 0, -1, row, col, board);
        flip_line(who, 0, 1, row, col, board);

        flip_line(who, 1, -1, row, col, board);
        flip_line(who, 1, 0, row, col, board);
        flip_line(who, 1, 1, row, col, board);

    }

    function send_game_update(socket, game_id, message) {
        /* Check for existing game with this id */

        if (('undefined' === typeof games[game_id]) || !games[game_id]) {
            /* No game yet, make it */
            console.log('no game exists. creating ' + game_id + ' for ' + socket.id);
            games[game_id] = create_new_game();
        }

        /* Only 2 players in a game */
        var roomObj
        var numClients
        do {
            roomObj = io.sockets.adapter.rooms[game_id]
            numClients = roomObj.length;
            if (numClients > 2) {
                console.log('Too many clients in room: ' + game_id + ' #: ' + numClients)
                if (games[game_id].player_white.socket == roomObj.sockets[0]) {
                    games[game_id].player_white.socket = '';
                    games[game_id].player_white.username = '';
                } else if (games[game_id].player_black.socket == roomObj.sockets[0]) {
                    games[game_id].player_black.socket = '';
                    games[game_id].player_black.username = '';
                }
                /* kick someone */
                var kick_recipient = Object.keys(roomObj.sockets)[0];
                io.of('/').connected[kick_recipient].leave(game_id)
            }


        } while ((numClients - 1) > 2)

        /* Assign this socket a color */
        if ((games[game_id].player_white.socket != socket.id) && (games[game_id].player_black.socket != socket.id)) {
            console.log('Player isn\'t assigned a color: ' + players[socket.id].username)
            if (games[game_id].player_black.socket != '' && games[game_id].player_white.socket != '') {
                games[game_id].player_black.socket = ''
                games[game_id].player_black.username = ''
                games[game_id].player_white.socket = ''
                games[game_id].player_white.username = ''
            }
        }

        /* Assign White */
        if (games[game_id].player_white.socket == '') {
            if (games[game_id].player_black.socket != socket.id) {
                console.log('Assigning ' + players[socket.id].username + ' white')
                games[game_id].player_white.socket = socket.id
                games[game_id].player_white.username = players[socket.id].username
            }
        }

        /* Assign Black */
        if (games[game_id].player_black.socket == '') {
            if (games[game_id].player_white.socket != socket.id) {
                console.log('Assigning ' + players[socket.id].username + ' black')
                games[game_id].player_black.socket = socket.id
                games[game_id].player_black.username = players[socket.id].username
            }
        }

        /* Send game update */
        var success_data = {
            result: 'success',
            game: games[game_id],
            message: message,
            game_id: game_id
        };

        io.in(game_id).emit('game_update', success_data);

        /* Check to see if game is over */

        var row, col;
        var count = 0;
        var black_final = 0
        var white_final = 0
        for (row = 0; row < 8; row++) {
            for (col = 0; col < 8; col++) {
                if (games[game_id].legal_moves[row][col] != ' ') {
                    count++
                }
                if (games[game_id].board[row][col] === 'b') {
                    black_final++
                }
                if (games[game_id].board[row][col] === 'w') {
                    white_final++
                }
            }
        }

        /* Send game over message */
        if (count == 0) {
            var winner = 'tie game';
            if (black_final > white_final) {
                winner = 'Black'
            }
            if (black_final < white_final) {
                winner = 'White'
            }

            var success_data = {
                result: 'success',
                game: games[game_id],
                game_id: game_id,
                winner: winner
            }

            log('player count in game ' + game_id + ': ' + numClients)
            io.in(game_id).emit('game_over', success_data)


            /* Delete old games */
            setTimeout(function (id) {
                return function () {
                    delete games[id];
                }
            }(game_id), 3600 * 1000)

        }

    }
})