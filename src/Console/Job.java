public class Job {
  String instruction;
  Object data1 = null;
  Object data2 = null;

  public Job(String instruction, Object data1) {
    this.instruction = instruction;
    this.data1 = data1;
  }

  public Job(String instruction, Object data1, Object data2) {
    this.instruction = instruction;
    this.data1 = data1;
    this.data1 = data2;
  }
}
