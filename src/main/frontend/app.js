import {create, parseCreationOptionsFromJSON, supported} from "@github/webauthn-json/browser-ponyfill";

async function generatePasskey() {
    const creationOptionsTextarea = document.getElementById("creation-options");
    // const credentialCreateOptions = creationOptionsTextarea.value;

    const username = document.getElementById("username").value;
    const displayName = document.getElementById("displayName").value;
    const publicKeyCredential = await getPublicKeyCredential(username, displayName);

    creationOptionsTextarea.value = JSON.stringify(publicKeyCredential);
    // Return encoded PublicKeyCredential to server
    const registerResponse = await fetchPostAsJson("/register-credential", JSON.stringify(publicKeyCredential));

    creationOptionsTextarea.value += "\n\n\n" + JSON.stringify(registerResponse);
}

async function getPublicKeyCredential(name, displayName) {
    if (sessionStorage.getItem("publicKeyCredential")) {
        return JSON.parse(sessionStorage.getItem("publicKeyCredential"));
    }

    // Make the call that returns the credentialCreateJson above
    const credentialCreateOptions = await fetchPostAsJson(
        "/create-credential",
        JSON.stringify({name, displayName})
    );

    // Call WebAuthn ceremony using webauthn-json wrapper
    const cco = parseCreationOptionsFromJSON(credentialCreateOptions.publicKeyCredentialCreationOptions);
    const publicKeyCredential = await create(cco);

    const cachedCredential = publicKeyCredential.toJSON();
    sessionStorage.setItem("publicKeyCredential", JSON.stringify(cachedCredential));
    return cachedCredential;
}

function fetchPostAsJson(input, body) {
    return fetch(input, {
        method: "POST",
        body: body,
        headers: {"Content-Type": "application/json"}
    }).then(resp => resp.json());
}

window.addEventListener("load", () => {
    document.getElementById("generate-passkey").addEventListener("click", generatePasskey)
});