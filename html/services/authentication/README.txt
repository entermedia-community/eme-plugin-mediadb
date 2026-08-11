curl -s -X POST "http://localhost:8080/finder/mediadb/services/authentication/token.json" \
  --data-urlencode "grant_type=password" \
  --data-urlencode "username=admin" \
  --data-urlencode "password=admin"

# Step 1: request a code (emails it; also writes it to the templogincode table)
curl -s -X POST "http://localhost:8080/finder/mediadb/services/authentication/sendusercode.json" \
  --data-urlencode "email=<a real user's email>"

# Step 2: redeem the 6-digit code from that email within 15 minutes
curl -s -X POST "http://localhost:8080/finder/mediadb/services/authentication/token.json" \
  --data-urlencode "grant_type=otp" \
  --data-urlencode "email=<same email>" \
  --data-urlencode "code=<6-digit code from the email>"
