class Department extends Zone {
  Faculty faculty;

  public Department(Faculty faculty, String name) {
    super(name);
    this.faculty = faculty;
  }
}
