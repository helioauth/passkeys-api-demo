(() => {
  // node_modules/@github/webauthn-json/dist/esm/webauthn-json.browser-ponyfill.js
  function base64urlToBuffer(baseurl64String) {
    const padding = "==".slice(0, (4 - baseurl64String.length % 4) % 4);
    const base64String = baseurl64String.replace(/-/g, "+").replace(/_/g, "/") + padding;
    const str = atob(base64String);
    const buffer = new ArrayBuffer(str.length);
    const byteView = new Uint8Array(buffer);
    for (let i = 0; i < str.length; i++) {
      byteView[i] = str.charCodeAt(i);
    }
    return buffer;
  }
  function bufferToBase64url(buffer) {
    const byteView = new Uint8Array(buffer);
    let str = "";
    for (const charCode of byteView) {
      str += String.fromCharCode(charCode);
    }
    const base64String = btoa(str);
    const base64urlString = base64String.replace(/\+/g, "-").replace(
      /\//g,
      "_"
    ).replace(/=/g, "");
    return base64urlString;
  }
  var copyValue = "copy";
  var convertValue = "convert";
  function convert(conversionFn, schema, input) {
    if (schema === copyValue) {
      return input;
    }
    if (schema === convertValue) {
      return conversionFn(input);
    }
    if (schema instanceof Array) {
      return input.map((v) => convert(conversionFn, schema[0], v));
    }
    if (schema instanceof Object) {
      const output = {};
      for (const [key, schemaField] of Object.entries(schema)) {
        if (schemaField.derive) {
          const v = schemaField.derive(input);
          if (v !== void 0) {
            input[key] = v;
          }
        }
        if (!(key in input)) {
          if (schemaField.required) {
            throw new Error(`Missing key: ${key}`);
          }
          continue;
        }
        if (input[key] == null) {
          output[key] = null;
          continue;
        }
        output[key] = convert(
          conversionFn,
          schemaField.schema,
          input[key]
        );
      }
      return output;
    }
  }
  function derived(schema, derive) {
    return {
      required: true,
      schema,
      derive
    };
  }
  function required(schema) {
    return {
      required: true,
      schema
    };
  }
  function optional(schema) {
    return {
      required: false,
      schema
    };
  }
  var publicKeyCredentialDescriptorSchema = {
    type: required(copyValue),
    id: required(convertValue),
    transports: optional(copyValue)
  };
  var simplifiedExtensionsSchema = {
    appid: optional(copyValue),
    appidExclude: optional(copyValue),
    credProps: optional(copyValue)
  };
  var simplifiedClientExtensionResultsSchema = {
    appid: optional(copyValue),
    appidExclude: optional(copyValue),
    credProps: optional(copyValue)
  };
  var credentialCreationOptions = {
    publicKey: required({
      rp: required(copyValue),
      user: required({
        id: required(convertValue),
        name: required(copyValue),
        displayName: required(copyValue)
      }),
      challenge: required(convertValue),
      pubKeyCredParams: required(copyValue),
      timeout: optional(copyValue),
      excludeCredentials: optional([publicKeyCredentialDescriptorSchema]),
      authenticatorSelection: optional(copyValue),
      attestation: optional(copyValue),
      extensions: optional(simplifiedExtensionsSchema)
    }),
    signal: optional(copyValue)
  };
  var publicKeyCredentialWithAttestation = {
    type: required(copyValue),
    id: required(copyValue),
    rawId: required(convertValue),
    authenticatorAttachment: optional(copyValue),
    response: required({
      clientDataJSON: required(convertValue),
      attestationObject: required(convertValue),
      transports: derived(
        copyValue,
        (response) => {
          var _a;
          return ((_a = response.getTransports) == null ? void 0 : _a.call(response)) || [];
        }
      )
    }),
    clientExtensionResults: derived(
      simplifiedClientExtensionResultsSchema,
      (pkc) => pkc.getClientExtensionResults()
    )
  };
  var credentialRequestOptions = {
    mediation: optional(copyValue),
    publicKey: required({
      challenge: required(convertValue),
      timeout: optional(copyValue),
      rpId: optional(copyValue),
      allowCredentials: optional([publicKeyCredentialDescriptorSchema]),
      userVerification: optional(copyValue),
      extensions: optional(simplifiedExtensionsSchema)
    }),
    signal: optional(copyValue)
  };
  var publicKeyCredentialWithAssertion = {
    type: required(copyValue),
    id: required(copyValue),
    rawId: required(convertValue),
    authenticatorAttachment: optional(copyValue),
    response: required({
      clientDataJSON: required(convertValue),
      authenticatorData: required(convertValue),
      signature: required(convertValue),
      userHandle: required(convertValue)
    }),
    clientExtensionResults: derived(
      simplifiedClientExtensionResultsSchema,
      (pkc) => pkc.getClientExtensionResults()
    )
  };
  function createRequestFromJSON(requestJSON) {
    return convert(base64urlToBuffer, credentialCreationOptions, requestJSON);
  }
  function createResponseToJSON(credential) {
    return convert(
      bufferToBase64url,
      publicKeyCredentialWithAttestation,
      credential
    );
  }
  async function create(options) {
    const response = await navigator.credentials.create(
      options
    );
    response.toJSON = () => createResponseToJSON(response);
    return response;
  }

  // app.js
  async function generatePasskey() {
    const creationOptionsTextarea = document.getElementById("creation-options");
    const username = document.getElementById("username").value;
    const displayName = document.getElementById("displayName").value;
    const publicKeyCredential = await getPublicKeyCredential(username, displayName);
    creationOptionsTextarea.value = JSON.stringify(publicKeyCredential);
    const registerResponse = await fetchPostAsJson("/register-credential", JSON.stringify(publicKeyCredential));
    creationOptionsTextarea.value += "\n\n\n" + JSON.stringify(registerResponse);
  }
  async function getPublicKeyCredential(name, displayName) {
    if (sessionStorage.getItem("publicKeyCredential")) {
      return JSON.parse(sessionStorage.getItem("publicKeyCredential"));
    }
    const credentialCreateOptions = await fetchPostAsJson(
      "/create-credential",
      JSON.stringify({ name, displayName })
    );
    const cco = createRequestFromJSON(credentialCreateOptions.publicKeyCredentialCreationOptions);
    const publicKeyCredential = await create(cco);
    const cachedCredential = publicKeyCredential.toJSON();
    sessionStorage.setItem("publicKeyCredential", JSON.stringify(cachedCredential));
    return cachedCredential;
  }
  function fetchPostAsJson(input, body) {
    return fetch(input, {
      method: "POST",
      body,
      headers: { "Content-Type": "application/json" }
    }).then((resp) => resp.json());
  }
  window.addEventListener("load", () => {
    document.getElementById("generate-passkey").addEventListener("click", generatePasskey);
  });
})();
//# sourceMappingURL=app.js.map
