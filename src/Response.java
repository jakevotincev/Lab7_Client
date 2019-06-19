import java.io.Serializable;

class Response implements Serializable {
    private static final long serialVersionUID = 3;
    private Object response;
    private Status status;
    Response(Object response) {
        this.response = response;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    Object getResponse() {
        return response;
    }
}
