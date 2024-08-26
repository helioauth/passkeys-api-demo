import {create, parseCreationOptionsFromJSON} from "@github/webauthn-json/browser-ponyfill";
import * as webauthnJson from "@github/webauthn-json";
import jdenticon from "jdenticon/standalone";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";

const API_PREFIX = "/v1/credentials";

const API_PATHS = {
    SIGNUP_CREATE: `${API_PREFIX}/signup/start`,
    SIGNUP_FINISH: `${API_PREFIX}/signup/finish`,

    SIGNIN_START: `${API_PREFIX}/signin/start`,
    SIGNIN_FINISH: `${API_PREFIX}/signin/finish`,

    CREDENTIALS_ADD_START: `${API_PREFIX}/add/start`,
    CREDENTIALS_ADD_FINISH: `${API_PREFIX}/add/finish`
};

async function signUpWithPasskey() {
    const email = document.getElementById("email").value;

    try {
        const createCredentialResponse = await createCredential(email);

        const registrationResponse = await fetchPostAsJson(API_PATHS.SIGNUP_FINISH,
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
            API_PATHS.SIGNUP_CREATE,
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
        if (resp.ok && !resp.redirected) {
            return resp.json();
        }

        throw resp;
    });
}

async function signInWithPasskey() {
    const email = document.getElementById("email").value;

    try {
        const optionsResponse = await fetchPostAsJson(API_PATHS.SIGNIN_START, {
            name: email
        });

        const publicKeyCredential = await webauthnJson.get(JSON.parse(optionsResponse.credentialsGetOptions));

        const signinResponse = await fetchPostAsJson(API_PATHS.SIGNIN_FINISH, {
            requestId: optionsResponse.requestId,
            publicKeyCredentialWithAssertion: JSON.stringify(publicKeyCredential)
        });

    } catch (response) {
        // TODO fix error handling
        if (response.ok && response.redirected) {
            window.location.replace(response.url);
        } else if (response.status >= 400 && response.status <= 499) {
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

async function addPasskeyAction() {
    const successMessage = document.getElementById("success-message");
    const errorMessage = document.getElementById("error-message");

    successMessage.classList.add("d-none");
    errorMessage.classList.add("d-none");

    try {
        const credentialCreateOptions = await fetchPostAsJson(
            API_PATHS.CREDENTIALS_ADD_START,
            {}
        );
        const cco = parseCreationOptionsFromJSON(JSON.parse(credentialCreateOptions.publicKeyCredentialCreationOptions));
        const publicKeyCredential = await create(cco);

        const registrationResponse = await fetchPostAsJson(API_PATHS.CREDENTIALS_ADD_FINISH,
            {
                requestId: credentialCreateOptions.requestId,
                publicKeyCredential: JSON.stringify(publicKeyCredential.toJSON())
            }
        );

        if (registrationResponse.requestId === createCredentialResponse.requestId) {
            successMessage.innerText = "New passkey added successfully!"
            successMessage.classList.remove("d-none");
        }
    } catch (e) {
        successMessage.classList.add("d-none");
        errorMessage.innerText = "Something went wrong!"
        errorMessage.classList.remove("d-none");
        // throw new Error("Oops, something went wrong");
    }
}

window.addEventListener("load", () => {
    const signUpForm = document.getElementById("signup-form");
    if (signUpForm !== null) {
        signUpForm.addEventListener("submit", signUpFormValidate)
    }

    jdenticon();
    dayjs.extend(relativeTime);
    for (const dateElement of document.getElementsByClassName("time-ago")) {
        dateElement.innerHTML = dayjs(dateElement.getAttribute("data-date")).fromNow();
    }

    const addPasskeyButton = document.getElementById("add-passkey-button");
    if (addPasskeyButton !== null) {
        addPasskeyButton.addEventListener("click", addPasskeyAction);
    }
});