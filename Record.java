class Record {
    int index;
    int year;

    public Record(int index, int year) {
        this.index = index;
        this.year = year;
    }

    @Override
    public String toString() {
        return index + "," + year;
    }
}
