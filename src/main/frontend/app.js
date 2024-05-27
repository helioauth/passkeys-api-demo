import {create, parseCreationOptionsFromJSON} from "@github/webauthn-json/browser-ponyfill";
import * as webauthnJson from "@github/webauthn-json";

async function signUpWithPasskey() {
    const email = document.getElementById("email").value;

    try {
        const createCredentialResponse = await createCredential(email);

        const registrationResponse = await fetchPostAsJson("/register-credential",
            {
                requestId: createCredentialResponse.requestId,
                publicKeyCredential: JSON.stringify(createCredentialResponse.publicKeyCredential)
            }
        );

        if (registrationResponse.requestId === createCredentialResponse.requestId) {
            document.getElementById("success-message").innerText = "Thanks for signing up! You can sign in with your passkey below."
            document.getElementById("success-message").classList.remove("d-none");
        }
    } catch (e) {
        if (e.message.length > 0) {
            document.getElementById("error-message").innerText = e.message;
            document.getElementById("error-message").classList.remove("d-none");
        }
    }
}

async function createCredential(name) {
    try {
        const credentialCreateOptions = await fetchPostAsJson(
            "/create-credential",
            {name}
        );
        const cco = parseCreationOptionsFromJSON(JSON.parse(credentialCreateOptions.publicKeyCredentialCreationOptions));
        const publicKeyCredential = await create(cco);

        return {
            requestId: credentialCreateOptions.requestId,
            publicKeyCredential: publicKeyCredential.toJSON()
        };
    } catch (response) {
        if (response.status >= 400 && response.status <= 499) {
            const errorDetails = await response.json();
            document.getElementById("email-invalid-message").innerText = (errorDetails.message ?? "Invalid email address");
            document.getElementById("email").classList.add("is-invalid");
            throw new Error();
        }

        if (response.code !== null && response.code === 0 && response.name === "NotAllowedError") {
            throw new Error("Authentication cancelled.");
        }

        throw new Error("Oops, something went wrong");
    }
}

function fetchPostAsJson(input, body) {
    return fetch(input, {
        method: "POST",
        body: JSON.stringify(body),
        headers: {"Content-Type": "application/json"}
    })
    .catch(reason => console.log("Connection error: " + reason.toString()))
    .then(resp => {
        if (resp.ok) {
            return resp.json();
        }

        throw resp;
    });
}

async function signInWithPasskey() {
    const email = document.getElementById("email").value;

    try {
        const optionsResponse = await fetchPostAsJson("/signin-credential-options", {
            name: email
        });

        const publicKeyCredential = await webauthnJson.get(JSON.parse(optionsResponse.credentialsGetOptions));

        const signinResponse = await fetchPostAsJson("/signin-validate-key", {
            requestId: optionsResponse.requestId,
            publicKeyCredentialWithAssertion: JSON.stringify(publicKeyCredential)
        });

        document.getElementById("success-message").innerText = "Welcome " + signinResponse.username + "!";
        document.getElementById("success-message").classList.remove("d-none");

    } catch (response) {
        // TODO fix error handling
        if (response.status >= 400 && response.status <= 499) {
            const errorDetails = await response.json();
            document.getElementById("email-invalid-message").innerText = (errorDetails.message ?? "Invalid email address");
            document.getElementById("email").classList.add("is-invalid");
        } else if (response.code !== null && response.code === 0 && response.name === "NotAllowedError") {
            document.getElementById("error-message").innerText = "Authentication cancelled.";
            document.getElementById("error-message").classList.remove("d-none");
        } else if (response.message !== null) {
            document.getElementById("error-message").innerText = response.message;
            document.getElementById("error-message").classList.remove("d-none");
        }
    }

}

function signUpFormValidate(event) {
    event.preventDefault();
    if (event.target.checkValidity() === false) {
        event.target.reportValidity();
        return;
    }

    for (const inputElement of document.getElementsByTagName("input")) {
        inputElement.classList.remove("is-invalid");
    }

    document.getElementById("error-message").classList.add("d-none");
    document.getElementById("success-message").classList.add("d-none");

    switch (event.submitter.id) {
        case "signup-button":
            return signUpWithPasskey(event);
        case "signin-button":
            return signInWithPasskey(event);
    }
}

window.addEventListener("load", () => {
    const signUpForm = document.getElementById("signup-form");
    if (signUpForm !== null) {
        signUpForm.addEventListener("submit", signUpFormValidate)
    }
});