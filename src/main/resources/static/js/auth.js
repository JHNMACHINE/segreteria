const AUTH_BASE_URL = "/api/v1/auth";

async function login({ formId, emailId, passwordId, errorId, redirectUrl }) {
  const form = document.getElementById(formId);
  const errorDiv = document.getElementById(errorId);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    errorDiv.textContent = "";

    const email = document.getElementById(emailId).value.trim();
    const password = document.getElementById(passwordId).value;

    try {
      const response = await fetch(`${AUTH_BASE_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);
        window.location.href = redirectUrl;
      } else {
        const errText = await response.text();
        errorDiv.textContent = errText || "Email o password non validi.";
      }
    } catch {
      errorDiv.textContent = "Errore di connessione al server.";
    }
  });
}

async function register({
  formId,
  nomeId,
  cognomeId,
  emailId,
  passwordId,
  ruoloId,
  matricolaId,
  errorId,
  successId,
  redirectUrl,
}) {
  const form = document.getElementById(formId);
  const errorDiv = document.getElementById(errorId);
  const successDiv = document.getElementById(successId);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    errorDiv.textContent = "";
    successDiv.textContent = "";

    const nome = document.getElementById(nomeId).value.trim();
    const cognome = document.getElementById(cognomeId).value.trim();
    const email = document.getElementById(emailId).value.trim();
    const password = document.getElementById(passwordId).value;
    const ruolo = document.getElementById(ruoloId).value;
    const matricola = document.getElementById(matricolaId).value.trim();

    try {
      const response = await fetch(`${AUTH_BASE_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nome, cognome, email, password, ruolo, matricola }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);
        successDiv.textContent = "Registrazione completata! Sei loggato.";
        if (redirectUrl) {
          setTimeout(() => window.location.href = redirectUrl, 1500);
        }
      } else {
        const errText = await response.text();
        errorDiv.textContent = errText || "Registrazione fallita.";
      }
    } catch {
      errorDiv.textContent = "Errore di rete durante la registrazione.";
    }
  });
}

async function refreshToken() {
  const oldToken = localStorage.getItem("token");
  if (!oldToken) return null;

  try {
    const response = await fetch(`${AUTH_BASE_URL}/refresh`, {
      method: "POST",
      headers: { Authorization: "Bearer " + oldToken },
    });
    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);
      return data.token;
    } else {
      console.error("Refresh token fallito");
      return null;
    }
  } catch (err) {
    console.error("Errore nel refresh token", err);
    return null;
  }
}

async function logout() {
  localStorage.removeItem("token");
  window.location.href = "/";
}

async function fetchWithAuth(url, options = {}) {
  let token = localStorage.getItem("token");
  if (!token) {
    logout();
    return null;
  }

  options.headers = {
    ...options.headers,
    "Authorization": "Bearer " + token,
    "Content-Type": "application/json"
  };

  let response = await fetch(url, options);

  if (response.status === 401) {
    const refreshResult = await refreshToken();
    if (refreshResult.success) {
      token = refreshResult.token;
      localStorage.setItem("token", token);
      options.headers["Authorization"] = "Bearer " + token;
      response = await fetch(url, options);

      if (response.status === 401) {
        logout();
        return null;
      }
    } else {
      logout();
      return null;
    }
  }

  return response;
}