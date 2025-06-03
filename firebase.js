import { initializeApp } from "https://www.gstatic.com/firebasejs/11.8.1/firebase-app.js";
  import { getAnalytics } from "https://www.gstatic.com/firebasejs/11.8.1/firebase-analytics.js";
  // TODO: Add SDKs for Firebase products that you want to use
  // https://firebase.google.com/docs/web/setup#available-libraries

  // Your web app's Firebase configuration
  // For Firebase JS SDK v7.20.0 and later, measurementId is optional
  const firebaseConfig = {
    apiKey: "AIzaSyBdXhyQX4ay_SAr4kccdhkFrBIyM-xPWn4",
    authDomain: "bakpiaq-ce0d0.firebaseapp.com",
    projectId: "bakpiaq-ce0d0",
    storageBucket: "bakpiaq-ce0d0.firebasestorage.app",
    messagingSenderId: "194724746659",
    appId: "1:194724746659:web:a8e6d4ca3c378523103616",
    measurementId: "G-W80GK7XWNG"
  };

  // Initialize Firebase
  const app = initializeApp(firebaseConfig);
  const analytics = getAnalytics(app);