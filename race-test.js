import http from 'k6/http'

export const options = {
    vus: 2,
    iterations: 2
};

const aliceId = "11111111-1111-1111-1111-111111111111"
const bobId = "22222222-2222-2222-2222-222222222222"

export default function(){
    const url = "http://localhost:8080/transfers";
    const payload = JSON.stringify({
        fromAccountId: aliceId,
        toAccountId: bobId,
        amount: 500.00,
        idempotencyKey: `race-${__VU}-${Date.now()}`,
    })

    const params = { headers: {"Content-Type": "application/json"}};

    const res = http.post(url, payload, params);

    console.log(`VU ${__VU}: status=${res.status} body=${res.body}`);
}
