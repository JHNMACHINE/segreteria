const AUTH_BASE_URL = "/api/v1/auth";

export async function login({ formId, emailId, passwordId, errorId, redirectUrl,expectedRole}) {
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
         const token = data.token;

      // decodifica token per estrarre ruolo
        let decoded;
      try {
         decoded = JSON.parse(atob(token.split('.')[1]));

      } catch(err) {
        console.error("Token non valido:", err);
        errorDiv.textContent = "Errore token.";
        return;
      }
      const ruolo = decoded.ruolo;
      if (expectedRole && ruolo !== expectedRole) {
        errorDiv.textContent = `Accesso negato: sei ${ruolo}, non ${expectedRole.toLowerCase()}.`;
        return;
      }
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

export async function registerSegretario(){

}

export async function registerStudent({
  formId,
  nomeId,
  cognomeId,
  emailId,
  passwordId,
  ruoloId,
  matricolaId,  // Aggiungi l'ID della matricola
  pianoDiStudiId,  // Aggiungi l'ID del piano di studi
  residenzaId,
  dataDiNascitaId,
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
    const ruolo = "STUDENTE";
    const matricola = document.getElementById(matricolaId).value;
    const pianoDiStudi = document.getElementById(pianoDiStudiId).value;
    const dataDiNascita = document.getElementById(dataDiNascitaId).value;
    const residenza = document.getElementById(residenzaId).value;

    try {
      const response = await fetch(`${AUTH_BASE_URL}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nome, cognome, email, password, ruolo, matricola, pianoDiStudi, dataDiNascita, residenza }), // Invia matricola e piano di studi solo per gli studenti
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

export async function registerDocente({
    formId,
     nomeId,
     cognomeId,
     emailId,
     passwordId,
     ruoloId,
     pianoDiStudiId,  // Aggiungi l'ID del piano di studi
     residenzaId,
     dataDiNascitaId,
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
        const ruolo = "DOCENTE";
        const pianoDiStudi = document.getElementById(pianoDiStudiId).value;
        const dataDiNascita = document.getElementById(dataDiNascitaId).value;
        const residenza = document.getElementById(residenzaId).value;

        try {
          const response = await fetch(`${AUTH_BASE_URL}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nome, cognome, email, password, ruolo, pianoDiStudi, dataDiNascita, residenza }),
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