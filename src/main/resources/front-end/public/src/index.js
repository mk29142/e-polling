(function() {

  $('#create').click(function(e) {
    e.preventDefault();
    window.location.href='/create';
  });

  $('#vote').click(function(e) {
      e.preventDefault();
      window.location.href='/vote';
    });
})();
