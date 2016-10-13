import React, { Component } from 'react';
import './App.css';

import BoxList from './BoxList';

class App extends Component {
  constructor(props) {
    super(props);
    this.addBox = this.addBox.bind(this);
    this.state = {
      boxes: []
    };
  }

  addBox(e) {
    var newBox = {
      text: 'New Box ' + this.state.boxes.length,
      id: Date.now()
    };

    this.setState((prevState) => ({
      boxes: prevState.boxes.concat(newBox)
    }));
  }

  render() {
    return (
      <div>
        <button onClick={ this.addBox }>Add a box</button>
        <BoxList boxes={ this.state.boxes } />
      </div>
    );
  }
}

export default App;
