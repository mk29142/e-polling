import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//file that will hold the Quad V algorithm
class QuadV {

	public static void main(String[] args){
	}

}

class Argument {

  public List<Argument> children;
  public boolean vote;
  public String argumentTitle;
  public boolean isSupporter;
  
  public Argument(boolean vote, String argumentTitle, boolean isSupporter){
	  this.argumentTitle = argumentTitle;
	  this.vote = vote;
	  this.isSupporter = isSupporter;
  }
  
  public void setChildren(List<Argument> children){
	  this.children = children;
  }
    
  public boolean isStable(){
	  if(vote){

		  if(!hasVotedForSupporter() && hasVotedForAttacker()){
			  return false;
		  }
		  
	  } else {
		  
		  if(!hasVotedForAttacker() && hasVotedForSupporter()){
			  return false;
		  }
		  
	  }
	  
	  
	  return false;
  }

  public boolean hasVotedForSupporter(){

	  for(Iterator<Argument> i = children.iterator(); i.hasNext();) {
		  Argument child = i.next();
		  if(child.isSupporter == true && child.vote == true){
			  return true;
		  }
	  }
	  return false;
  }

  public boolean hasVotedForAttacker(){
	  for(Iterator<Argument> i = children.iterator(); i.hasNext();) {
		  Argument child = i.next();
		  if(child.isSupporter == false && child.vote == true){
			  return true;
		  }
	  }

	  return false;
  }


}


