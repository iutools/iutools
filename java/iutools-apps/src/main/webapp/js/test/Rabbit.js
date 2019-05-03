// Inherit from Animal by specifying "extends Animal"
class Rabbit extends Animal {
	
  run(speed) {
	  console.log(`${this.name} run, invoked from Rabbit!... invoking the parent version`);
	  super.run(speed);
  }
	
  hide() {
    console.log(`${this.name} hides!`);
  }
}