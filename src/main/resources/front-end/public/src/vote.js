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
  }];

  var title = $('#debate-title');
  var question = $('#question');
  var slider = $('#question-slider');
  var reason = $('#opinion-area');
  var issue = questions[0];
  var counter = 1;

  title.html(issue.text);
})();
