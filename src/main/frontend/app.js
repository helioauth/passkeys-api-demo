import {create, parseCreationOptionsFromJSON} from "@github/webauthn-json/browser-ponyfill";
import * as webauthnJson from "@github/webauthn-json";
import jdenticon from "jdenticon/standalone";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";

const DEMO_APP_ID = window.helioauth_demo.APP_ID ?? "";
const API_DOMAIN = window.helioauth_demo.API_URL ?? "http://localhost:8080/";
const API_PREFIX = `${API_DOMAIN}v1`;

const API_PATHS = {
    SIGNUP_START: `${API_PREFIX}/signup/start`,
    SIGNUP_FINISH: `/signup`,

    SIGNIN_START: `${API_PREFIX}/signin/start`,
    SIGNIN_FINISH: `/login`,

    CREDENTIALS_ADD_START: `${API_PREFIX}/credentials/add/start`,
    CREDENTIALS_ADD_FINISH: `${API_PREFIX}/credentials/add/finish`
};

const USER_ABORTED_ERROR = "user_aborted";

let authAbortController = new AbortController();

async function signUpWithPasskey() {
    const displayName = document.getElementById("displayName").value;
    const email = document.getElementById("email").value;

    try {
        const signUpResponse = await fetchPostAsJson(
            API_PATHS.SIGNUP_START,
            {name: email}
        );

        const publicKeyCredential = await webauthnJson.create(JSON.parse(signUpResponse.options));

        await fetchPostAsJson(API_PATHS.SIGNUP_FINISH,
            {
                requestId: signUpResponse.requestId,
                publicKeyCredential: JSON.stringify(publicKeyCredential),
                displayName,
                email
            }
        );

        document.getElementById("signin-email").value = email;

        showSuccessMessage("Thanks for signing up! You can sign in with your passkey below.");
    } catch (response) {
        if (response.status >= 400 && response.status <= 499) {
            const errorDetails = await response.json();
            document.getElementById("email-invalid-message").innerText = (errorDetails.message ?? "Invalid email address");
            document.getElementById("email").classList.add("is-invalid");
        } else if (response.code !== null && response.code === 0 && response.name === "NotAllowedError") {
            // Authentication canceled by user.
        } else {
            showErrorMessage("Oops, something went wrong");
        }

    }
}

async function handleUserAuthenticationResponse(response) {
    if (response.ok && response.redirected) {
        window.location.replace(response.url);
    } else if (response.status >= 400 && response.status <= 499) {
        const errorDetails = await response.json();
        showErrorMessage(errorDetails.message ?? "Invalid email address");
    } else if (response.code !== null && response.code === 0 && response.name === "NotAllowedError") {
        showErrorMessage("Authentication cancelled.");
    } else if (response.message !== null) {
        showErrorMessage(response.message);
    }
}

async function signInWithPasskey() {
    const email = document.getElementById("signin-email").value;

    try {
        const optionsResponse = await fetchPostAsJson(API_PATHS.SIGNIN_START, {
            name: email
        });

        if (!optionsResponse.accountExists) {
            showErrorMessage("No account found with this email address.");
            return;
        }

        const publicKeyCredential = await webauthnJson.get(JSON.parse(optionsResponse.options));

        await fetchPostAsJson(API_PATHS.SIGNIN_FINISH, {
            requestId: optionsResponse.requestId,
            publicKeyCredentialWithAssertion: JSON.stringify(publicKeyCredential)
        });

        showSuccessMessage("Sign-in success! Redirecting to dashboard!");
    } catch (response) {
        await handleUserAuthenticationResponse(response);
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

    hideErrorMessage();
    hideSuccessMessage();

    switch (event.submitter.id) {
        case "signup-button":
            return signUpWithPasskey(event);
        case "signin-button":
            return signInWithPasskey(event);
    }
}

async function addPasskeyAction() {
    hideSuccessMessage();
    hideErrorMessage();

    try {
        const name = prompt("Enter a name for your passkey");
        const startResponse = await fetchPostAsJson(
            API_PATHS.CREDENTIALS_ADD_START,
            {name}
        );

        const credentialCreationOptions = parseCreationOptionsFromJSON(startResponse.options);
        const publicKeyCredential = await create(credentialCreationOptions);

        await fetchPostAsJson(API_PATHS.CREDENTIALS_ADD_FINISH,
            {
                requestId: startResponse.requestId,
                publicKeyCredential: JSON.stringify(publicKeyCredential.toJSON())
            }
        );

        showSuccessMessage("New passkey added successfully!");
    } catch (e) {
        hideSuccessMessage();
        showErrorMessage("Something went wrong!");
    }
}

async function initiateAutofill() {
    const signInEmail = document.getElementById("signin-email");
    if (signInEmail === null) {
        return;
    }

    signInEmail.addEventListener("input", () => {
        if (!authAbortController.signal.aborted) {
            authAbortController.abort(USER_ABORTED_ERROR);
        }
    })


    const email = document.getElementById("email");
    if (email !== null) {
        email.addEventListener("input", () => {
            if (!authAbortController.signal.aborted) {
                authAbortController.abort(USER_ABORTED_ERROR);
            }
        });
    }

    const mediationAvailable = () => {
        const pubKeyCred = PublicKeyCredential;
        return !!(typeof pubKeyCred.isConditionalMediationAvailable === "function" &&
            pubKeyCred.isConditionalMediationAvailable());
    };

    if (mediationAvailable()) {
        try {
            authAbortController = new AbortController();
            const optionsResponse = await fetchPostAsJson(API_PATHS.SIGNIN_START, {});

            const options = JSON.parse(optionsResponse.options);

            const publicKeyCredential = await webauthnJson.get({
                publicKey: options.publicKey,
                mediation: "conditional",
                signal: authAbortController.signal
            });

            await fetchPostAsJson(API_PATHS.SIGNIN_FINISH, {
                requestId: optionsResponse.requestId,
                publicKeyCredentialWithAssertion: JSON.stringify(publicKeyCredential)
            });

            showSuccessMessage("Sign-in success! Redirecting to dashboard!");
        } catch (error) {
            if (typeof error === "string" && error === USER_ABORTED_ERROR) {
                return;
            }

            if (error instanceof DOMException && error.name === "AbortError") {
                return;
            }

            await handleUserAuthenticationResponse(error);
        }
    }
}

window.addEventListener("load", () => {
    const signUpForm = document.getElementById("signup-form");
    if (signUpForm !== null) {
        signUpForm.addEventListener("submit", signUpFormValidate)
    }

    const signInForm = document.getElementById("signin-form");
    if (signInForm !== null) {
        signInForm.addEventListener("submit", signUpFormValidate)
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

    initiateAutofill();
});

function fetchPostAsJson(input, body) {
    return fetch(input, {
        method: "POST",
        body: JSON.stringify(body),
        headers: {"Content-Type": "application/json", "X-App-Id": DEMO_APP_ID}
    })
        .catch(reason => console.log("Connection error: " + reason.toString()))
        .then(resp => {
            if (resp.ok && !resp.redirected) {
                return resp.json();
            }

            throw resp;
        });
}

function showErrorMessage(errorMessage) {
    document.getElementById("error-message").innerText = errorMessage;
    document.getElementById("error-message").classList.remove("d-none");
}

function hideErrorMessage() {
    document.getElementById("error-message").classList.add("d-none");
}

function showSuccessMessage(successMessage) {
    document.getElementById("success-message").innerText = successMessage;
    document.getElementById("success-message").classList.remove("d-none");
}

function hideSuccessMessage() {
    document.getElementById("success-message").classList.add("d-none");
}