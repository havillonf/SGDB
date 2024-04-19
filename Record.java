class Record {
    int year;
    double value;

    public Record(int year, double value) {
        this.year = year;
        this.value = value;
    }

    @Override
    public String toString() {
        return year + "," + value;
    }
}
