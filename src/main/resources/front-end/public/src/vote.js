(function() {
  let pollId = window.location.href.substring(
    window.location.href.lastIndexOf('/') + 1);

  $.get('/boxes/' + pollId, function(data) {
    let questions = JSON.parse(data);

    let title = $('#debate-title');
    let question = $('#question');
    let options = $('input[name=options]');
    let reason = $('#opinion-area');
    let issue = questions[0];
    let counter = 0;
    let currQ = questions[counter];

    let userId = '';
    // Do a post to /user so we are able to log their ip address once to put questions in the database
    $.ajax({
      type: 'POST',
      url: '/user/' + pollId,
      data: {
        userId: userId,
      },
      success: function(data) {
        console.log('id: ' + data);
        userId = data;
      },
      error: function(data) {
        console.log('Error in user post: ', data);
      },
    });

    let dynamicCounter;
    let currConflictSet;

    let dynamicData = {
      questions: [questions], // questions to update answers for,
                              // initialised to be all questions with head as first value
      nextLevel: 0, // next level to be searched for inconsistencies
      userId: userId,
    };

    $('#finalQ').hide();
    title.html(issue.text);
    let num = parseInt(currQ.id) + 1;
    question.html(num + ': ' + currQ.text);
    setNavList();
    setActive([counter]);

    $('input[name="options"]').change(function(e) {
      e.preventDefault();
      setTimeout(changeQuestion, 500);
    });

    $('#nextQ').click(function(e) {
      e.preventDefault();
      changeQuestion();
    });

    $('#finalQ').click(function(e) {
      e.preventDefault();
      currQ.support = options.filter(':checked').val();
      currQ.reason = reason.val();

      if (currQ.support) {
        $('#vote-yes').prop('checked', currQ.support === 'yes');
        $('#vote-no').prop('checked', currQ.support === 'no');
      } else {
        $('#vote-yes').prop('checked', false);
        $('#vote-no').prop('checked', false);
      }

      if (allAnswered(questions)) {

        // Send this ajax post for first answers and receive inconsistencies from first level
        submitDynamicData();

        // A modal will pop up with dynamic questions from data obj
        // on last round of dynamic questions modal will show submit
        // window.location.href = '/results/' + data;
        $('#dynamicQuestionSubmit').click(function(e) {
          e.preventDefault();
          for (let i = 1; i < dynamicData.questions[dynamicCounter].length; i++) {
            let val = $('input[name=' + i + ']:checked', '#dynamicQuestionForm').val();
            dynamicData.questions[dynamicCounter][i].support = val;
          }

          dynamicCounter++;

          // Send this ajax post when we want inconsistencies for next level
          if (dynamicCounter >= dynamicData.questions.length) {
            submitDynamicData();
          } else {
            currConflictSet = dynamicData.questions[dynamicCounter];
            displayModal();
          }
        });
      }

    });

    $('#nav-list a').click(function(e) {
      counter = parseInt(e.currentTarget.text) - 2;
      changeQuestion();
    });

     function submitDynamicData() {
      console.log("submitDyanmicData()");
      dynamicData.userId = userId;
      $.ajax({
        type: 'POST',
        url: '/answers/' + pollId,
        data: JSON.stringify(dynamicData),
        dataType: 'json',
        success: function(data) {
          console.log(data);
          if (data != 'STOP') {
            dynamicData.questions = data.dynamicQuestions;
            dynamicData.nextLevel = data.nextLevel;

            dynamicCounter = 0;
            currConflictSet = dynamicData.questions[dynamicCounter];

            displayModal();
          } else {
            window.location.href = '/results/' + pollId;
          }
        },
        error: function() {
          console.log('Error in submitting dynamic data');
        },
      });
    }

    function setNavList() {
      let nav = $('#nav-list');

      for (let i = 0; i < questions.length; i++) {
        let num = parseInt(questions[i].id) + 1;
        nav.append('<li class="waves-effect"><a ' +
        'value="' +
        questions[i].id +
        '">' +
        num +
        '</a></li>');
      }
    };

    //not using this function yet
    function allAnswered(questions) {
       let unansweredIndices = [];

       for (let i = 0; i < questions.length; i++) {
         if (questions[i].support === undefined) {
           unansweredIndices.push(i);
         }
       }
       //Highlight all the wrong questions in red
       setActive(unansweredIndices);
       return unansweredIndices.length === 0;
    }

    function displayModal() {
      $('#questions').html('');

      $('#conflictTitle').text('CONFLICT! Your answers to the following questions are inconsistent with the question: ' +
        currConflictSet[0].text + '. Please change your answer or give a reason why you answered the way you did.');

      for(let i = 1; i < currConflictSet.length; i++) {
        let support = currConflictSet[i].support;
        createQuestion(currConflictSet[i].text, i);
        $('#q' + i + '-' + support).prop('checked', true);
      }

      $('#dynamicModal').openModal({
        dismissible: false,
      });
    }

    //At the moment, we are making all red unanswered questions lose redness
    //on any question click, we want the rest to stay red until answered
    function setActive(indices) {
      let nav = $('#nav-list');
      let children = nav.children();
      children.removeClass('active');

      indices.forEach(function (index) {
        let active = children.eq(index);
        active.addClass('active');   
      });
      
    }

    function changeQuestion() {
      currQ.support = options.filter(':checked').val();
      currQ.reason = reason.val();

      if (counter > questions.length - 2) {
        return;
      }

      currQ = questions[++counter];
      var num = parseInt(currQ.id) + 1;
      question.text(num + ': ' + currQ.text);
      reason.val(currQ.reason);

      if (currQ.support) {
        $('#vote-yes').prop('checked', currQ.support === 'yes');
        $('#vote-no').prop('checked', currQ.support === 'no');
      } else {
        $('#vote-yes').prop('checked', false);
        $('#vote-no').prop('checked', false);
      }

      if (currQ.id === questions.length - 1) {
        $('#nextQ').hide();
        $('#finalQ').show();
      } else {
        $('#nextQ').show();
        $('#finalQ').hide();
      }

      setActive([counter]);
    }

    function createQuestion(question, counter) {
      let q = '<div id="' + question + '">' +
      '<p>' + question + '</p>' +
      '<input type="radio" id="q' + counter + '-yes" name="' + counter + '" value="yes">' +
      '<label for="q' + counter +'-yes">Yes</label>   &nbsp; &nbsp; &nbsp;  ' +
      '<input type="radio" id="q' + counter + '-no" name="' + counter + '" value="no">' +
      '<label for="q' + counter +'-no">No</label>' +
      '</div>';

      $('#questions').append(q);
    }
  });
})();
