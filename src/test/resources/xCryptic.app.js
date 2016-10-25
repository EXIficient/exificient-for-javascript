var express = require('express'),
    app = express(),
    port = Number(process.env.PORT || 3000),
    io = require('socket.io').listen(app.listen(port)),
    colors = require('colors'),
    mongoose = require('mongoose'),
    server = '[Server] '.white.bold,
    error = '[Error] '.red.bold,
    users = {},
    admins = {},
    saveMsg;
 
console.log(server + 'listening @ localhost:' + port);
 
mongoose.connect('mongodb://localhost/chat', function(errormsg){
    if (errormsg) {
        console.log(error + errormsg);
    } else {
        console.log(server + 'Connected to MongoDB');
    }
});
 
var schema = mongoose.Schema({
    msg: String,
    date: {type: Date, default: Date.now}
}),
    userschema = mongoose.Schema({
    username: String,
    password: String,
    salt: String,
    isAdmin: Boolean,
    mute: Boolean,
    ban: Boolean,
    banReason: String
});
 
var chat = mongoose.model('message', schema),
    userdb = mongoose.model('userdb', userschema);
 
app.use('/images', express.static('images'));
app.use('/css', express.static('css'));
app.use('/mp3', express.static('mp3'));
app.use('/js', express.static('js'));
app.get('/', function (req, res) {
    res.sendFile(__dirname + '/chat.html');
});
 
var commands = require('./commands.js'),
    functions = require('./functions.js');
 
io.on('connection', function (socket) {
    globalSocket = socket;
    var query = chat.find({});
    query.sort('-date').limit(50).exec(function(errormsg, msgs) { // Load the last 50 messages in order
        if (errormsg) console.log(error + errormsg);
        socket.emit('load messages', msgs);
    });
 
    socket.on('username', function(data) {
        var userCheck = userdb.each({ username: data });
        userCheck.sort().limit(1).exec(function(errormsg, result) {
            if (result.length == 1) {
                socket.emit('result', result[0].salt);
            }
        });
    });
 
    socket.on('register', function(registerData, callback) {
        functions.register(registerData, callback);
    });
 
    socket.on('user login', function (data, callback) {
        functions.login(data, callback, socket, io, admins, users);
    });
 
    socket.on('disconnect', function () {
        if (!socket.username) return;
        delete users[socket.username]; // Remove user from list
        try {delete admins[socket.admin];} catch(err) {}
        functions.updateNicknames(io, users);
        console.log(server + 'User Left: ' + socket.username);
        io.emit('chat message', '<font color="#4E7ACC"><b>[Server]</b> ' + socket.username + ' has left</font><br/>');
        saveMsg = new chat({ msg: '<font color="#4E7ACC"><b>[Server]</b> ' + socket.username + ' has left</font><br/>' });
        saveMsg.save(function (errormsg) { if (errormsg) console.log(error + errormsg); });
    });
     
    socket.on('chat message', function (msg) {
        if (!msg == "") { // Check to make sure a message was entered
            if (!socket.username == "") { // Check to make sure the client has a username
                if(msg.substr(0, 9) === '/commands') { // Check for whisper
                    commands.commands(socket, users);
                } else if (msg.substr(0, 3) === '/w ') {
                    commands.whisper(msg, socket, users);
                } else {
                    if (socket.username in admins) {
                        if (msg.substr(0, 6) === '/smsg ') {
                            commands.adminsMsg(msg, socket, io);
                        } else if (msg.substr(0, 6) === '/kick ') {
                            commands.adminKick(msg, socket, io, users);
                        } else if (msg.substr(0, 5) === '/ban ') {
                            commands.adminBan(msg, socket, io, users);
                        } else if (msg.substr(0, 7) === '/unban ') {
                            commands.adminUnban(msg, socket, users);
                        } else {
                            functions.adminMessage(msg, socket, io);
                        }
                    } else {
                        functions.message(msg, socket, io);
                    }
                }
            }
        }
    });
});
 
var stdin = process.stdin, stdout = process.stdout;
 
stdin.resume();
stdin.on('data', function (data) {
    var input = data.toString().trim(); // Take out any unecessary spaces
    if (input == 'shutdown') { // Shutdown command
        console.log(server + 'Shutting down...');
        io.emit('disconnect');
 
        mongoose.disconnect;
        process.exit();
    } else if (input.substr(0, 5) === 'kick ') { // Kick command
        commands.cmdKick(input, io, users);
    } else if (input.substr(0, 4) === 'ban ') { // Ban command
        commands.cmdBan(input, io, users);
    } else if (input.substr(0, 6) === 'unban ') { // Unban command
        commands.cmdUnban(input);
    } else if (input.substr(0, 6) === 'admin ') { // Admin command
        commands.cmdAdmin(input, io, users, admins);
    } else { // Anything else that's entered is sent as a server message
        io.emit('chat message', '<font color="#5E97FF"><b>[Server]</b> ' + input + '</font><br/>');
        saveMsg = new chat({ msg: '<font color="#5E97FF"><b>[Server]</b> ' + input + '</font><br/>' });
    }
    try {
        saveMsg.save(function (errormsg) { if (errormsg) console.log(error + errormsg); });
    } catch (err) {} // no messages to save (command didn't save a message)
});
