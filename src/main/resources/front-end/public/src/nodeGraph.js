(function() {
  let pollId = window.location.href.substring(
    window.location.href.lastIndexOf('/') + 1);
  let svg = d3.select('svg'),
      width = +svg.attr('width'),
      height = +svg.attr('height');

  let color = d3.scaleOrdinal(d3.schemeCategory10);

  let simulation = d3.forceSimulation()
    .force('link', d3.forceLink().id(function(d) {
      return d.id;
    }))
    .force('charge', d3.forceManyBody())
    .force('center', d3.forceCenter(width / 4, height / 4));

  d3.json('/nodeGraph/' + pollId, function(err, graph) {
    if (err) throw err;

    let link = svg.append('g')
      .attr('class', 'links')
      .selectAll('line')
      .data(graph.links)
      .enter().append('line')
      .attr('stroke-width', function(d) {
        return Math.sqrt(d.value);
      });

    let node = svg.append('g')
      .attr('class', 'nodes')
      .selectAll('circle')
      .data(graph.nodes)
      .enter().append('circle')
      .attr('r', function(d) {
        return d.radius * 25;
      })
      .attr('fill', function(d) {
        return color(d.group);
      })
      .attr('fill-opacity', function(d) {
        return d.opacity;
      })
      .call(d3.drag()
        .on('start', dragstarted)
        .on('drag', dragged)
        .on('end', dragended));

    node.append('title')
      .text(function(d) {
        function twodp(num) {
          return Math.round(num * 100);
        }
        return d.text + '\n' +
          'Base Score (τ): ' + twodp(d.opacity) + '\n' +
          'Score (σ): ' + twodp(d.radius);
      });

    simulation
      .nodes(graph.nodes)
      .on('tick', ticked);

    simulation.force('link')
      .links(graph.links);

    function ticked() {
      link.attr('x1', function(d) {
        return d.source.x * 2;
      })
      .attr('y1', function(d) {
        return d.source.y * 2;
      })
      .attr('x2', function(d) {
        return d.target.x * 2;
      })
      .attr('y2', function(d) {
        return d.target.y * 2;
      });

      node.attr('cx', function(d) {
        return d.x * 2;
      })
      .attr('cy', function(d) {
        return d.y * 2;
      });
    }
  });

  function dragstarted(d) {
    if (!d3.event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
  }

  function dragged(d) {
    d.fx = d3.event.x;
    d.fy = d3.event.y;
  }

  function dragended(d) {
    if (!d3.event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
  }
})();
