package kamel.capstone.nosqlnode.data.model;

public class IDDTO {
    private long _id;

    public IDDTO() {
    }

    public IDDTO(long id) {
        _id = id;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    @Override
    public String toString() {
        return String.valueOf(_id);
    }
}
