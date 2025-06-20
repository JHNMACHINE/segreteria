const AUTH_BASE_URL = "/api/v1/auth";

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
          credentials: "include"   // <-- importante!
        });


        if (response.ok) {
          // Login ok, reindirizza
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
        const value = getValue(field);
        if (!value) {
          errorDiv.textContent = "Compila tutti i campi.";
          return;
        }
        formData[field] = value;
      }


      if (!formData.email || !formData.password) {
        errorDiv.textContent = "Compila tutti i campi.";
        return;
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        errorDiv.textContent = "Inserisci un'email valida.";
        return;
      }


      try {
        const response = await fetch(`${AUTH_BASE_URL}/register`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ...formData, ruolo }),
          credentials: "include"
        });

        if (response.ok) {
          const data = await response.json();
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
  try {
    const response = await fetch(`${AUTH_BASE_URL}/refresh`, {
      method: "POST",
      credentials: "include"  // manda i cookie con la richiesta
    });
    if (response.ok) {
      // la risposta non contiene più il token in body
      // puoi decidere se leggere un messaggio o niente
      return true;
    } else {
      console.error("Refresh token fallito");
      return false;
    }
  } catch (err) {
    console.error("Errore nel refresh token", err);
    return false;
  }
}

export async function logout() {
  try {
    await fetch(`${AUTH_BASE_URL}/logout`, { // TODO logout nel backend
      method: 'POST',
      credentials: 'include'  // <-- fondamentale per cancellare il cookie
    });
  } catch (err) {
    console.error("Errore nel logout:", err);
  } finally {
    window.location.href = "/";
  }
}