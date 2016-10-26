(function() {
  var issues = [{
    id: 0,
    text: 'The big issue'
  }, {
    id: 1,
    text: 'The bigger issue'
  }, {
    id: 2,
    text: 'The biggest issue'
  }];

  function setIssueList() {
    var issueList = $('#issue-list');

    for (var i = 0; i < issues.length; i++) {
      issueList.append('<a class="collection-item"' +
      'href="/vote/' +
      issues[i].id +
      '">' +
      issues[i].text +
      '</a>');
    }
  };

  setIssueList();
})();
