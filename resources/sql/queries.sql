-- name: create-user!
-- creates a new user record
INSERT INTO users
(id, first_name, last_name, email, passwd)
VALUES (:id, :first_name, :last_name, :email, :passwd)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- name: get-user
-- retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id
