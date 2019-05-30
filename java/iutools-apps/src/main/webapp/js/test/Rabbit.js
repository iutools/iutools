// Inherit from Animal by specifying "extends Animal"
class Rabbit extends Animal {
	
  run(speed) {
	  console.log(`${this.name} run, invoked (Rabbit impl.)! In turn, this method is now invoking the parent version of taht method.`);
	  super.run(speed);
  }
	
  hide() {
    console.log(`${this.name} hides (Rabbit impl.)!`);
  }
  
  hopAlong() {
	  console.log(`${this.name} hops along (Rabbit impl.)!`);
	  this.run(5);
	  this.jump();
  }
}