import React, { Component } from 'react';
import Draggable from 'react-draggable';
import './BoxFactory.css';

class BoxFactory extends Component {
  render() {
    return (
      <div>
        {this.props.boxes.map(box => (
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
            <div key={box.id} className="handle">{box.text}</div>
          </Draggable>
        ))}
      </div>
    );
  }
}
export default BoxFactory;
