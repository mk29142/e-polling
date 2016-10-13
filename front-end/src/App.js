import React, { Component } from 'react';
import './App.css';
import Draggable, {DraggableCore} from 'react-draggable';

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
          <button className="col s1" onClick={ this.addBox }>Add Issue</button>
          <button className="col s2" onClick={ this.addBox }>Add Answer</button>
          <button className="col s3" onClick={ this.addBox }>Add Pro Argument</button>
          <button className="col s4" onClick={ this.addBox }>Add Con Argument</button>
        </div>
        <BoxList boxes={ this.state.boxes } />
        <Draggable
          axis="both"
          handle=".handle"
          defaultPosition={{x: 0, y: 0}}
          position={null}
          grid={[25, 25]}
          zIndex={100}
          onStart={this.handleStart}
          onDrag={this.handleDrag}
          onStop={this.handleStop}>
          <div>
            <div className="handle">Drag from here</div>
            <div>This readme is really dragging on...</div>
          </div>
        </Draggable>
      </div>
    );
  }
}

export default App;
