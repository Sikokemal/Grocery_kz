const express = require("express");
const app = express();
// This is your test secret API key.
const stripe = require("stripe")('sk_test_51OZ8uMEjS44wb3THI1NbAt1SlbI1CrWnIbsHLiuChthVvE0LUERK9RWcEVl9h5mjfmBM3qF6bEeCfGfz2czybDPd00uLnonpn4sk_test_51OZ8uMEjS44wb3THI1NbAt1SlbI1CrWnIbsHLiuChthVvE0LUERK9RWcEVl9h5mjfmBM3qF6bEeCfGfz2czybDPd00uLnonpn4');

app.use(express.static("public"));
app.use(express.json());

const calculateOrderAmount = (items) => {
  // Calculate the order total on the server to prevent
  // people from directly manipulating the amount on the client
  let total = 0;
  items.forEach((item) => {
    total += item.amount;
  });
  return total;
};

app.post("/create-payment-intent", async (req, res) => {
  const { items } = req.body;

  // Create a PaymentIntent with the order amount and currency
  const paymentIntent = await stripe.paymentIntents.create({
    amount: calculateOrderAmount(items),
    currency: "usd",
    // In the latest version of the API, specifying the `automatic_payment_methods` parameter is optional because Stripe enables its functionality by default.
    automatic_payment_methods: {
      enabled: true,
    },
  });

  res.send({
    clientSecret: paymentIntent.client_secret,
    // [DEV]: For demo purposes only, you should avoid exposing the PaymentIntent ID in the client-side code.
    dpmCheckerLink: `https://dashboard.stripe.com/settings/payment_methods/review?transaction_id=${paymentIntent.id}`,
  });
});


app.listen(4242, () => console.log("Node server listening on port 4242!"));