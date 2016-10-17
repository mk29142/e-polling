import React, { Component } from 'react';
import Draggable from 'react-draggable';

import './App.css';
import BoxFactory from './BoxFactory';

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
      id: 'box' + this.state.boxes.length
    };

    this.setState((prevState) => ({
      boxes: prevState.boxes.concat(newBox)
    }));
  }

  render() {
    return (
      <div>
        <div className="row">
          <div className="col s3">
            <button className="waves-effect waves-light btn" onClick={ this.addBox }>Add Issue</button>
          </div>
          <div className="col s3">
            <button className="waves-effect waves-light btn" onClick={ this.addBox }>Add Answer</button>
          </div>
          <div className="col s3">
            <button className="waves-effect waves-light btn" onClick={ this.addBox }>Add Pro Argument</button>
          </div>
          <div className="col s3">
            <button className="waves-effect waves-light btn" onClick={ this.addBox }>Add Con Argument</button>
          </div>
        </div>
        <BoxFactory boxes={ this.state.boxes } />
      </div>
    );
  }
}

export default App;
