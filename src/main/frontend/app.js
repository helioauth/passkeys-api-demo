import {create, parseCreationOptionsFromJSON} from "@github/webauthn-json/browser-ponyfill";

async function signUpWithPasskey() {
    for (const inputElement of document.getElementsByTagName("input")) {
        inputElement.classList.remove("is-invalid");
    }
    document.getElementById("error-message").classList.add("d-none");

    const email = document.getElementById("email").value;

    try {
        const createCredentialResponse = await createCredential(email);

        await fetchPostAsJson("/register-credential", JSON.stringify(
            {
                requestId: createCredentialResponse.requestId,
                publicKeyCredential: JSON.stringify(createCredentialResponse.publicKeyCredential)
            }
        ));
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
            JSON.stringify({name})
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

        throw new Error("Oops, something went wrong");
    }
}

function fetchPostAsJson(input, body) {
    return fetch(input, {
        method: "POST",
        body: body,
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

window.addEventListener("load", () => {
    const signUpBtn = document.getElementById("signup-button")

    if (signUpBtn !== null) {
        signUpBtn.addEventListener("click", signUpWithPasskey)
    }
});