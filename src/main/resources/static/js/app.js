const API_URL = window.location.origin //"http://localhost:8080/";
let token = null;
let conversationId = null;
let stompClient = null;
let currentUsername = null;
let subscription = null;
let searchTimeout;

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
        currentUsername = username;
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
    loadConversations();
}

// ------------------- LOGOUT -------------------
function logout() {
    localStorage.removeItem("token");
    token = null;
    document.getElementById("auth").style.display = "block";
    document.getElementById("app").style.display = "none";
    //document.getElementById("chat").style.display = "none";
}

// ------------------- SEARCH USERS -------------------
async function searchUsers() {
    clearTimeout(searchTimeout);

    searchTimeout = setTimeout(async () => {

        const query = document.getElementById("searchInput").value;

        if (!query) {
            document.getElementById("results").innerHTML = "";
            return;
        }

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

            /*button.style.display = "block";
            button.style.width = "100%";
            button.style.marginTop = "10px";*/

            button.onclick = () => {
                startConversation(user.id, user.username);
            };

            resultsList.appendChild(button);
        });
    },300);
}

// ------------------- START CONVERSATION -------------------
async function startConversation(userId, username) {

    const response = await fetch(`${API_URL}/api/conversations/${userId}`, {
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

    document.getElementById("results").innerHTML = "";
    document.getElementById("searchInput").value = "";
    // تحميل الرسائل القديمة
    await loadMessages();
    // الاتصال بالويب سوكيت
    connectWebSocket();
}

// ------------------- CONNECT TO WEBSOCKET -------------------
function connectWebSocket() {

    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect(
        { Authorization: "Bearer " + token },
        function(frame) {

            console.log("Connected: " + frame);

            // إلغاء الاشتراك القديم
            if (subscription) {
                subscription.unsubscribe();
            }

            // الاشتراك الجديد
            subscription = stompClient.subscribe(
                `/topic/conversation/${conversationId}`,
                function(message) {

                    const msg = JSON.parse(message.body);

                    if(msg.senderUsername === currentUsername){
                        showMessage("Me", msg.content);
                    }else{
                        showMessage(msg.senderUsername, msg.content);
                    }

                }
            );

        }
    );
}

// ------------------- SEND MESSAGE -------------------
function sendMessage() {

    if(!stompClient || !stompClient.connected){
        alert("WebSocket not connected yet");
        return;
    }

    const message = document.getElementById("messageInput").value;

    if (!message) return;

    stompClient.send(
        "/app/chat.send",
        {},
        JSON.stringify({
            content: message,
            conversationId: conversationId
        })
    );

    document.getElementById("messageInput").value = "";
}

// ------------------- SHOW MESSAGE -------------------
function showMessage(sender, content) {

    const messagesDiv = document.getElementById("messages");

    const messageEl = document.createElement("div");

    messageEl.classList.add("message");

    if (sender === "Me") {
        messageEl.classList.add("my-message");
        messageEl.innerText = content;
    } else {
        messageEl.classList.add("other-message");
        messageEl.innerText = sender + ": " + content;
    }

    messagesDiv.appendChild(messageEl);

    messagesDiv.scrollTop = messagesDiv.scrollHeight;
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

        if(msg.senderUsername === currentUsername){
            showMessage("Me", msg.content);
        }else{
            showMessage(msg.senderUsername, msg.content);
        }

    });
}

// -----------------------------------------------------------------------------------------------
function showRegister(){
    document.getElementById("loginBox").style.display = "none";
    document.getElementById("registerBox").style.display = "block";
}

function showLogin(){
    document.getElementById("registerBox").style.display = "none";
    document.getElementById("loginBox").style.display = "block";
}

async function register(){

    const username = document.getElementById("registerUsername").value;
    const displayName = document.getElementById("registerDisplayName").value;
    const email = document.getElementById("registerEmail").value;
    const password = document.getElementById("registerPassword").value;

    try{

        const response = await fetch(`${API_URL}/api/auth/register`,{
            method:"POST",
            headers:{
                "Content-Type":"application/json"
            },
            body: JSON.stringify({
                username:username,
                displayName:displayName,
                email:email,
                password:password
            })
        });

        if(!response.ok){
            alert("Registration failed");
            return;
        }

        alert("Account created successfully");

        showLogin();

    }catch(err){
        alert("Server error");
    }
}

async function loadConversations() {

    const response = await fetch("/api/conversations", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    const data = await response.json();

    const container = document.getElementById("conversations");
    container.innerHTML = "";

    data.forEach(conv => {

        const div = document.createElement("div");
        div.classList.add("conversation-item");

        div.innerHTML = `
            <strong>${conv.otherUsername}</strong><br>
            <small>${conv.lastMessage || "No messages yet"}</small>
        `;

        div.onclick = () => {
            selectConversation(div, conv);
        };

        container.appendChild(div);
    });
}

function selectConversation(element, conv) {

    // إزالة active من الكل
    document.querySelectorAll(".conversation-item")
        .forEach(el => el.classList.remove("active"));

    // إضافة active
    element.classList.add("active");

    // تعيين المحادثة
    conversationId = conv.conversationId;

    document.getElementById("chatTitle").innerText =
        "Chat with " + conv.otherUsername;

    //document.getElementById("chat").style.display = "block";

    loadMessages();
    connectWebSocket();
}

