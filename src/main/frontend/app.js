import {create, parseCreationOptionsFromJSON, supported} from "@github/webauthn-json/browser-ponyfill";

async function generatePasskey() {
    const creationOptionsTextarea = document.getElementById("creation-options");
    // const credentialCreateOptions = creationOptionsTextarea.value;

    const username = document.getElementById("username").value;
    const displayName = document.getElementById("displayName").value;
    const createCredentialResponse = await createCredential(username, displayName);

    creationOptionsTextarea.value = JSON.stringify(createCredentialResponse);
    // Return encoded PublicKeyCredential to server
    const registerResponse = await fetchPostAsJson("/register-credential", JSON.stringify(
        {
            requestId: createCredentialResponse.requestId,
            publicKeyCredential: JSON.stringify(createCredentialResponse.publicKeyCredential)
        }
    ));

    creationOptionsTextarea.value += "\n\n\n" + JSON.stringify(registerResponse);
}

async function createCredential(name, displayName) {
    const cacheKey = "publicKeyCredentialResponse";

    if (sessionStorage.getItem(cacheKey)) {
        return JSON.parse(sessionStorage.getItem(cacheKey));
    }

    // Make the call that returns the credentialCreateJson above
    const credentialCreateOptions = await fetchPostAsJson(
        "/create-credential",
        JSON.stringify({name, displayName})
    );

    // Call WebAuthn ceremony using webauthn-json wrapper
    const cco = parseCreationOptionsFromJSON(JSON.parse(credentialCreateOptions.publicKeyCredentialCreationOptions));
    const publicKeyCredential = await create(cco);

    const cachedCredentialResponse = {
        requestId: credentialCreateOptions.requestId,
        publicKeyCredential: publicKeyCredential.toJSON()
    };

    sessionStorage.setItem(cacheKey, JSON.stringify(cachedCredentialResponse));
    return cachedCredentialResponse;
}

function fetchPostAsJson(input, body) {
    return fetch(input, {
        method: "POST",
        body: body,
        headers: {"Content-Type": "application/json"}
    }).then(resp => resp.json());
}

window.addEventListener("load", () => {
    const generatePasskeyBtn = document.getElementById("generate-passkey")

    if (generatePasskeyBtn !== null) {
        generatePasskeyBtn.addEventListener("click", generatePasskey)
    }
});