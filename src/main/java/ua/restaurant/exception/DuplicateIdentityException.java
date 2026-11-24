package ua.restaurant.exception;

public class DuplicateIdentityException extends IllegalStateException {

    public DuplicateIdentityException(String identity, String repositoryName) {
        super("Item with identity '" + identity + "' already exists in repository '" + repositoryName + "'");
    }
}
