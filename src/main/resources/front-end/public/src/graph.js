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

    let colors = createColors(data.length);
    let backgroundColors = colors(0.4);
    let borderColors = colors(1);

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

    function randIntBetween(bottom, top) {
      return Math.floor(Math.random() * top) + bottom;
    }

    function createColors(num) {
      let colors = [];

      return function(transparency) {
        for (var i = 0; i < num; i++) {
          let color = 'rgba(';
          color += randIntBetween(30, 255) + ',';
          color += randIntBetween(50, 255) + ',';
          color += randIntBetween(10, 220) + ',';
          color += transparency + ')';
          colors.push(color);
        }

        return colors;
      }
    }

    function makeChart(name, labels, data) {
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
