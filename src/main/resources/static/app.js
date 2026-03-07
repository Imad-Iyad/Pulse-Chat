const API_URL = "http://localhost:8081";
let token = null;
let conversationId = null;
let stompClient = null;

// ------------------- LOGIN -------------------
async function login() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const errorEl = document.getElementById("error");

    errorEl.innerText = "";

    try {
        const response = await fetch(`${API_URL}/api/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            errorEl.innerText = "Invalid username or password";
            return;
        }

        const data = await response.json();
        token = data.token;

        localStorage.setItem("token", token);

        showApp();
    } catch (err) {
        errorEl.innerText = "Server connection failed";
    }
}

// ------------------- SHOW MAIN APP -------------------
function showApp() {
    document.getElementById("auth").style.display = "none";
    document.getElementById("app").style.display = "block";
}

// ------------------- LOGOUT -------------------
function logout() {
    localStorage.removeItem("token");
    token = null;
    document.getElementById("auth").style.display = "block";
    document.getElementById("app").style.display = "none";
    document.getElementById("chat").style.display = "none";
}

// ------------------- SEARCH USERS -------------------
async function searchUsers() {

    const query = document.getElementById("searchInput").value;

    const response = await fetch(`${API_URL}/api/users/search?query=${query}&page=0&size=10`, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    const data = await response.json();

    const resultsList = document.getElementById("results");
    resultsList.innerHTML = "";

    data.content.forEach(user => {

        const button = document.createElement("button");

        button.innerText = user.username + " (" + user.status + ")";

        button.style.display = "block";
        button.style.width = "100%";
        button.style.marginTop = "10px";

        button.onclick = () => {
            startConversation(user.id, user.username);
        };

        resultsList.appendChild(button);
    });
}

// ------------------- START CONVERSATION -------------------
async function startConversation(userId, username) {

        const response = await fetch(`http://localhost:8081/api/conversations/${userId}`, {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        if (!response.ok) {
            alert("Failed to create conversation");
            return;
        }

        const conversation = await response.json();

        conversationId = conversation.conversationId;

        document.getElementById("chatTitle").innerText = "Chat with " + username;
        document.getElementById("chat").style.display = "block";

        // تحميل الرسائل القديمة
        await loadMessages();
        // الاتصال بالويب سوكيت
        connectWebSocket();
}

// ------------------- CONNECT TO WEBSOCKET -------------------
function connectWebSocket() {
    const socket = new SockJS(`${API_URL}/ws`);
    stompClient = Stomp.over(socket);

    stompClient.connect(
        {
            Authorization: "Bearer " + token
        },
        function (frame){
        console.log('Connected: ' + frame);

        // Subscribe to messages from the conversation
        stompClient.subscribe(`/topic/conversation/${conversationId}`, function (message) {
            const msg = JSON.parse(message.body);
            showMessage(msg.senderId + ": " + msg.content);
        });
    });
}

// ------------------- SEND MESSAGE -------------------
function sendMessage() {

    const message = document.getElementById("messageInput").value;

    if (!message) return;

    const messageObj = {
        content: message,
        conversationId: conversationId
    };

    stompClient.send(
        "/app/chat.send",
        {},
        JSON.stringify(messageObj)
    );
    showMessage("Me: " + message);
    document.getElementById("messageInput").value = "";
}

// ------------------- SHOW MESSAGE -------------------
function showMessage(message) {
    const messagesDiv = document.getElementById("messages");
    const newMessage = document.createElement("div");
    newMessage.innerText = message;
    messagesDiv.appendChild(newMessage);
    messagesDiv.scrollTop = messagesDiv.scrollHeight; // auto-scroll
}

// ------------------- AUTO LOGIN IF TOKEN EXISTS -------------------
window.onload = () => {
    const savedToken = localStorage.getItem("token");
    if (savedToken) {
        token = savedToken;
        showApp();
    }
};

// ------------------ LOAD MESSAGES ---------------------------------
async function loadMessages() {

    const response = await fetch(
        `${API_URL}/api/conversations/${conversationId}/messages?page=0&size=50`,
        {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        }
    );

    const data = await response.json();
    console.log(data);

    const messagesDiv = document.getElementById("messages");
    messagesDiv.innerHTML = "";

    data.content.forEach(msg => {

        const messageEl = document.createElement("div");

        messageEl.innerText =
            msg.senderId + ": " + msg.content;

        messagesDiv.appendChild(messageEl);

    });
}