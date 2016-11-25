(function() {
  let pollId = window.location.href.substring(
    window.location.href.lastIndexOf('/') + 1);

  $.get('/graph/' + pollId, function(stringData) {
    let data = JSON.parse(stringData);
    let text = [];
    let sigmaScores = [];
    let tauScores = [];
    for (let i = 0; i < data.length; i++) {
      text.push(splitLines(data[i].text));

      sigmaScores.push(data[i].score * 100);
      tauScores.push(data[i].baseScore * 100);
    }

    let sigmaScore = new Chart(
      $('#sigmaScore'),
      makeChart('Score (σ)', text, sigmaScores));

    let tauScore = new Chart(
      $('#tauScore'),
      makeChart('Base Score (τ)', text, tauScores));

    function splitLines(text) {
      if (text.length < 30) {
        return [text];
      } else {
        let first30 = text.substring(0, 30);
        let splitIndex = first30.lastIndexOf(' ');
        return [
          text.substring(0, splitIndex),
          splitLines(text.substring(splitIndex))
        ];
      }
    }

    function makeChart(name, labels, data) {
      let backgroundColors = [
        'rgba(255, 99, 132, 0.2)',
        'rgba(54, 162, 235, 0.2)',
        'rgba(255, 206, 86, 0.2)',
        'rgba(75, 192, 192, 0.2)',
        'rgba(153, 102, 255, 0.2)',
        'rgba(255, 159, 64, 0.2)',
      ];

      let borderColors = [
        'rgba(255,99,132,1)',
        'rgba(54, 162, 235, 1)',
        'rgba(255, 206, 86, 1)',
        'rgba(75, 192, 192, 1)',
        'rgba(153, 102, 255, 1)',
        'rgba(255, 159, 64, 1)',
      ];

      let options = {
        responsive: true,
        maintainAspectRatio: true,
        scales: {
          xAxes: [{
            stacked: true,
          }],
          yAxes: [{
            stacked: true,
            ticks: {
              suggestedMax: 100,
              beginAtZero: true,
            },
          }],
        }
      };

      return {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: name,
            data: data,
            backgroundColor: backgroundColors,
            borderColor: borderColors,
            borderWidth: 1,
          }],
        },
        options: options
      };
    }
  });
})();
