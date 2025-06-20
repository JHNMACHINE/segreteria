const AUTH_BASE_URL = "/api/v1/auth";

export function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
}

const getValue = (id) => document.getElementById(id)?.value?.trim();


export async function login({ formId, emailId, passwordId, errorId, redirectUrl, expectedRole }) {
  const form = document.getElementById(formId);
  const errorDiv = document.getElementById(errorId);

  if (!form || !errorDiv) {
    console.error("Form o errorDiv non trovato.");
    return;
  }

  if (!form.dataset.listenerAttached) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      errorDiv.textContent = "";

      const email = getValue(emailId);
      const password = getValue(passwordId);

      if (!email || !password) {
        errorDiv.textContent = "Compila tutti i campi.";
        return;
      }

      try {
        const response = await fetch(`${AUTH_BASE_URL}/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password }),
        });

        if (response.ok) {
          const data = await response.json();
          const decoded = parseJwt(data.token);
          if (!decoded) {
            errorDiv.textContent = "Token non valido.";
            return;
          }
          const ruolo = decoded.ruolo;
          if (expectedRole && ruolo !== expectedRole) {
            errorDiv.textContent = "Accesso negato: Non sei autorizzato";
            return;
          }

          localStorage.setItem("token", data.token);
          window.location.href = redirectUrl;
        } else {
          const errText = await response.text();
          errorDiv.textContent = errText || "Email o password non validi.";
        }
      } catch (err) {
        console.error("Errore:", err);
        errorDiv.textContent = "Errore di connessione al server.";
      }
    });
    form.dataset.listenerAttached = "true";
  }
}

export function registerSegretario(params) {
  const {
    formId, nomeId, cognomeId, emailId, passwordId,
    matricolaId, dataDiNascitaId, residenzaId,
    errorId, successId, redirectUrl
  } = params;

  const fields = [nomeId, cognomeId, emailId, passwordId, matricolaId, dataDiNascitaId, residenzaId];
  return submitRegistration(formId, fields, "SEGRETARIO", errorId, successId, redirectUrl);
}

export function registerStudent(params) {
  const {
    formId, nomeId, cognomeId, emailId, passwordId,
    matricolaId, pianoDiStudiId, dataDiNascitaId, residenzaId,
    errorId, successId, redirectUrl
  } = params;

  const fields = [nomeId, cognomeId, emailId, passwordId, matricolaId, pianoDiStudiId, dataDiNascitaId, residenzaId];
  return submitRegistration(formId, fields, "STUDENTE", errorId, successId, redirectUrl);
}

export function registerDocente(params) {
  const {
    formId, nomeId, cognomeId, emailId, passwordId,
    pianoDiStudiId, dataDiNascitaId, residenzaId,
    errorId, successId, redirectUrl
  } = params;

  const fields = [nomeId, cognomeId, emailId, passwordId, pianoDiStudiId, dataDiNascitaId, residenzaId];
  return submitRegistration(formId, fields, "DOCENTE", errorId, successId, redirectUrl);
}

async function submitRegistration(formId, fields, ruolo, errorId, successId, redirectUrl) {
  const form = document.getElementById(formId);
  const errorDiv = document.getElementById(errorId);
  const successDiv = document.getElementById(successId);

  if (!form || !errorDiv || !successDiv) {
    console.error("Form o uno dei div non trovato.");
    return;
  }

  if (!form.dataset.listenerAttached) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      errorDiv.textContent = "";
      successDiv.textContent = "";

      const formData = {};
      for (const field of fields) {
        formData[field] = getValue(field);
      }

      if (!formData.email || !formData.password) {
        errorDiv.textContent = "Compila tutti i campi.";
        return;
      }

      try {
        const response = await fetch(`${AUTH_BASE_URL}/register`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ...formData, ruolo }),
        });

        if (response.ok) {
          const data = await response.json();
          localStorage.setItem("token", data.token);
          successDiv.textContent = "Registrazione completata!";
          if (redirectUrl) {
            setTimeout(() => window.location.href = redirectUrl, 1500);
          }
        } else {
          const errText = await response.text();
          errorDiv.textContent = errText || "Registrazione fallita.";
        }
      } catch (err) {
        console.error("Errore di rete:", err);
        errorDiv.textContent = "Errore di rete durante la registrazione.";
      }
    });
    form.dataset.listenerAttached = "true";
  }
}


export async function refreshToken() {
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