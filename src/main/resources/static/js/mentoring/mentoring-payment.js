document.getElementById('payment-button').addEventListener("click",
    paymentProcess);
document.getElementById('cancel-button').addEventListener('click', () => {
  cancelPaymentProcess();
});

function cancelPaymentProcess() {
  let currentUri = String(window.location.pathname);
  let requestUri = currentUri.replace(/\/payment$/, '/lock');
  axios.delete(requestUri).then(res => {
    if (res.data === 'SUCCESS') {
      location.href = currentUri.replace(/\/payment$/, '');
    }
  });
}

function paymentProcess() {

  let currentUri = String(window.location.pathname);

  let requestUri = currentUri + '-process';

  let nickname = document.getElementById('user-nickname').value;
  let email = document.getElementById('user-email').value;
  let tel = document.getElementById('user-tel').value;

  axios.post(requestUri, {
    nickname: nickname,
    email: email,
    tel: tel
  })
  .then(res => paymentRequest(res));
}

function paymentRequest(res) {
  let data = res.data;
  let impCode = data.imp_code;
  let merchantUid = data.merchant_uid;
  let amount = data.amount;
  let name = data.name;
  let buyerName = data.buyer_name;
  let buyerEmail = data.buyer_email;
  let buyerTel = data.buyer_tel;

  IMP.init(impCode);
  IMP.request_pay({
        pg: "tosspayments",
        pay_method: "toss",
        name: name,
        merchant_uid: merchantUid,
        amount: amount,
        buyer_tel: buyerTel,
        buyer_name: buyerName,
        buyer_email: buyerEmail
      },
      function (response) {
        paymentValidation(response, amount);
      }
  );
}

function paymentValidation(res, amount) {
  if (!res.error_code) {
    let impUid = res.imp_uid;
    let merchantUid = res.merchant_uid;

    axios.post('/payment/validation', {
      impUid: impUid,
      merchantUid: merchantUid,
      amount: amount
    }).then(res => {
      let body = res.data;
      if (res.status === 200 && body.validationResult === 'SUCCESS') {
        saveMentoringApplication(res, amount);
      } else {
        alert('결제 검증에 실패하였습니다.');
      }
    });
  } else {
    alert(res.error_msg);
  }
}

function saveMentoringApplication(res, amount) {
  //imp uid , amount, merchant uid,
  let startDateTime = document.getElementById('start').value;
  let endDateTime = document.getElementById('end').value;
  let currentUri = String(window.location.pathname);
  let requestUri = currentUri.replace(/\/payment$/, '/apply');
  axios.post(requestUri, {
    imp_uid: res.data.imp_uid,
    merchant_uid: res.data.merchant_uid,
    amount: amount,
    reserved_time: {
      from: startDateTime + ':00',
      to: endDateTime + ':00'
    }
  })
  .then(res => {
    if (res.status === 200) {
      window.location.href = '/payment/complete?order=' + res.data.order_uid;
    } else {
      alert('멘토링 신청내역 저장에 실패했습니다.');
    }
  });
}

