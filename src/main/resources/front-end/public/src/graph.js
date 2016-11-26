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

    let backgroundColors = createColors(data.length)(0.4);
    let borderColors = createColors(data.length)(1);

    google.charts.load('current', {
      'packages': ['bar']
    });
    google.charts.setOnLoadCallback(drawChart);

    function drawChart() {
      function combineScores() {
        let combined = [['Argument', 'Score (σ)', 'Base Score (τ)']];
        for (let i = 0; i < text.length; i++) {
          let newEntry = [];
          newEntry.push(text[i].join(''));
          newEntry.push(sigmaScores[i]);
          newEntry.push(tauScores[i]);
          combined.push(newEntry);
        }

        return combined;
      }

      let combined = combineScores();
      console.log(combined);
      let data = google.visualization.arrayToDataTable(combined);

      let options = {
        chart: {
          title: 'Comparison of scores',
          subtitle: 'Real scores and traditional scores',
        },
        bars: 'vertical', // Required for Material Bar Charts.
        axisTitlesPosition: 'in'
      };

      let chart = new google.charts.Bar(document.getElementById('barchart_material'));

      chart.draw(data, options);
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

    function randIntBetween(bottom, top) {
      return Math.floor(Math.random() * top) + bottom;
    }

    function hexToRGB(hex, alpha) {
      var r = parseInt(hex.slice(1, 3), 16),
          g = parseInt(hex.slice(3, 5), 16),
          b = parseInt(hex.slice(5, 7), 16);

      if (alpha) {
        return "rgba(" + r + ", " + g + ", " + b + ", " + alpha + ")";
      } else {
        return "rgb(" + r + ", " + g + ", " + b + ")";
      }
    }

    function createColors(num) {
      let colors = [];

      return function(transparency) {
        for (var i = 0; i < num; i++) {
          colors.push(d3.schemeCategory10[i % 10]);
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
            stacked: true
          }],
          yAxes: [{
            stacked: true,
            ticks: {
              suggestedMax: 100,
              beginAtZero: true,
            }
          }]
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
