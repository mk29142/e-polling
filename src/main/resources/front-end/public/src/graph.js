(function() {
  let pollId = window.location.href.substring(
    window.location.href.lastIndexOf('/') + 1);

  $.get('/graph/' + pollId, function(stringData) {
    let data = JSON.parse(stringData);
    let text = [];
    let sigmaScores = [];
    let tauScores = [];
    for (let i = 0; i < data.length; i++) {
      text.push(data[i].text);
      sigmaScores.push(data[i].score * 100);
      tauScores.push(data[i].baseScore * 100);
    }

    let sigmaScore = new Chart($('#sigmaScore'), {
      type: 'bar',
      data: {
        labels: text,
        datasets: [{
          label: 'Score (σ)',
          data: sigmaScores,
          backgroundColor: [
            'rgba(255, 99, 132, 0.2)',
            'rgba(54, 162, 235, 0.2)',
            'rgba(255, 206, 86, 0.2)',
            'rgba(75, 192, 192, 0.2)',
            'rgba(153, 102, 255, 0.2)',
            'rgba(255, 159, 64, 0.2)',
          ],
          borderColor: [
            'rgba(255,99,132,1)',
            'rgba(54, 162, 235, 1)',
            'rgba(255, 206, 86, 1)',
            'rgba(75, 192, 192, 1)',
            'rgba(153, 102, 255, 1)',
            'rgba(255, 159, 64, 1)',
          ],
          borderWidth: 1,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          xAxes: [{
            stacked: true,
          }],
          yAxes: [{
            stacked: true,
            ticks: {
              beginAtZero: true,
            },
          }],
        },
      },
    });

    let tauScore = new Chart($('#tauScore'), {
      type: 'bar',
      data: {
        labels: text,
        datasets: [{
          label: 'Base Score (τ)',
          data: tauScores,
          backgroundColor: [
            'rgba(255, 99, 132, 0.2)',
            'rgba(54, 162, 235, 0.2)',
            'rgba(255, 206, 86, 0.2)',
            'rgba(75, 192, 192, 0.2)',
            'rgba(153, 102, 255, 0.2)',
            'rgba(255, 159, 64, 0.2)',
          ],
          borderColor: [
            'rgba(255,99,132,1)',
            'rgba(54, 162, 235, 1)',
            'rgba(255, 206, 86, 1)',
            'rgba(75, 192, 192, 1)',
            'rgba(153, 102, 255, 1)',
            'rgba(255, 159, 64, 1)',
          ],
          borderWidth: 1,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          xAxes: [{
            stacked: true,
          }],
          yAxes: [{
            stacked: true,
            ticks: {
              beginAtZero: true,
            },
          }],
        },
      },
    });
  });
})();
