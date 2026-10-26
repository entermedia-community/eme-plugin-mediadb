# Step 1: request a code (emails it; also writes it to the templogincode table)
curl -s -X POST "http://localhost:8080/finder/mediadb/services/authentication/sendusercode.json" \
  --data-urlencode "email=<a real user's email>"
response:
{
	"response": {
		"status": "ok",
		"email": "support@openedit.org"
	}
}

# Step 2: redeem the 6-digit code from that email within 15 minutes
curl -s -X POST "http://localhost:8080/finder/mediadb/services/authentication/token.json" \
  --data-urlencode "grant_type=otp" \
  --data-urlencode "email=<same email>" \
  --data-urlencode "code=<6-digit code from the email>"


response:
{
	"access_token": "<access token>",
	"token_type": "Bearer",
	"expires_in": 86400,
	"refresh_token": "<refresh token>",
	"user": {
		"id": "admin",
		"firstname": "The",
		"lastname": "Administrator",
		"email": "support@entermediadb.org",
		"screenname": "Admin",
		"assetportrait": "http://localhost:8080/site/mediadb/services/module/asset/generated/Users/Shakil.A/image_picker_E3FCF644-58ED-4EFE-AFA7-CEC8386DF770-47636-000000A8B34B33E9.jpg/image200x200.webp"
	}
}

# Key login (SSH style, no password or emailed code once a device is registered)
# Step 1 (once per device, while logged in): the app generates its own Ed25519 key pair, keeps the private key
# in the device keystore, and uploads only the public key (base64url X.509). Keys do not expire; revoke them.
curl -s -X POST -H "Authorization: Bearer <existing token>" \
  "http://localhost:8080/finder/mediadb/services/authentication/registerkey.json" \
  --data-urlencode "publickey=<base64url public key>" --data-urlencode "label=Sarah's iPhone"
{ "response": {"status":"ok"}, "key_id": "9f2c..." }

# Step 2: sign "{username}\n{key_id}\n{timestamp-millis}\n{random nonce, 16-128 chars}" with the private key
# (base64url signature). Timestamp must be within 5 minutes of the server clock; each nonce works once.
curl -s -X POST "http://localhost:8080/finder/mediadb/services/authentication/token.json" \
  --data-urlencode "grant_type=key" --data-urlencode "username=admin" --data-urlencode "key_id=9f2c..." \
  --data-urlencode "timestamp=<millis>" --data-urlencode "nonce=<random>" --data-urlencode "signature=<signature>"
{ "access_token": "adminkeyexp9f2c....<expiry>.<hmac>", "token_type": "Bearer", "expires_in": 604800, "user": {...} }

# Manage keys: listkeys.json (GET) and revokekey.json (keyid=...). Revoking a key invalidates its tokens immediately.


# ---------------------------------------------------------------------------------------------
# The password-derived "entermedia key" (user + md542 + hash) and the emekey login cookie are gone.
# Credentials are now: a registered public key (token.json grant_type=key, 7 day Bearer token) or an
# application password (Authorization: Bearer emeapp.KEYID.SECRET). Browsers sign in with a key kept
# in IndexedDB (components/javascript/emedia/browserkey.js) and have no persistent login cookie.
# Mobile/desktop apps can redeem an emailed code and register their key in one call:
curl -s -X POST "http://localhost:8080/finder/mediadb/services/authentication/token.json" \
  --data-urlencode "grant_type=otp" --data-urlencode "email=<email>" --data-urlencode "code=<6 digit code>" \
  --data-urlencode "publickey=<base64url X.509 Ed25519 or P-256 public key>" --data-urlencode "label=Sarah's iPhone"
# -> access_token, key_id. From then on use grant_type=key. Email links carry a single use
#    login token (confirmlogin.html), never a key.
