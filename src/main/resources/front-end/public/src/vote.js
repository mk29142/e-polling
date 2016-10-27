(function() {
  var questions = [{
    id: 0,
    type: 'issue',
    text: 'The big issue',
    parent: null
  }, {
    id: 1,
    type: 'support',
    text: 'I support things',
    support: 50,
    reason: '',
    parent: 0
  }, {
    id: 2,
    type: 'attack',
    text: 'Boooo',
    support: 50,
    reason: '',
    parent: 0
  }, {
    id: 3,
    type: 'attack',
    text: 'The 3rd which attacks Boooo',
    support: 50,
    reason: '',
    parent: 2
  }];

  var title = $('#debate-title');
  var question = $('#question');
  var slider = $('#question-slider');
  var reason = $('#opinion-area');
  var issue = questions[0];
  var counter = 1;
  var currQ = questions[counter];

  $('#finalQ').hide();
  title.html(issue.text);
  question.html(currQ.text);
  setNavList();
  setActive();

  $('#nextQ').click(function(e) {
    e.preventDefault();
    changeQuestion();
  });

  $('#finalQ').click(function(e) {
    e.preventDefault();

    // Send back questions with ajax and redirect to results page
    window.location.href = '/results';
  });

  $('#nav-list .collection-item').click(function(e) {
    counter = e.target.attributes[1].value - 1;
    changeQuestion();
  });

  function setNavList() {
    var nav = $('#nav-list');

    for (var i = 1; i < questions.length; i++) {
      nav.append('<a class="collection-item"' +
      'value="' +
      questions[i].id +
      '">' +
      questions[i].text +
      '</a>');
    }
  };

  function setActive() {
    var nav = $('#nav-list');
    var children = nav.children();
    children.removeClass('active');

    var active = children.eq(counter - 1); // Nav-list doesn't contain the issue.
    active.addClass('active');
  }

  function changeQuestion() {
    currQ.support = slider.val();
    currQ.reason = reason.val();

    currQ = questions[++counter];
    question.html(currQ.text);
    slider.val(currQ.support);
    reason.val(currQ.reason);

    if (currQ.id === questions.length - 1) {
      $('#nextQ').hide();
      $('#finalQ').show();
    } else {
      $('#nextQ').show();
      $('#finalQ').hide();
    }

    setActive();
  }
})();
