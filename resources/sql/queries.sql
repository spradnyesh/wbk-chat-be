-- name: create-user!
-- creates a new user record
INSERT INTO users
(first_name, last_name, email, passwd)
VALUES (:firstname, :lastname, :email, :passwd)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :firstname, last_name = :lastname, email = :email
WHERE id = :id

-- name: get-all-users
-- retrieve all users
SELECT * FROM users

-- name: get-user
-- retrieve a user given the id
SELECT * FROM users
WHERE id = :id

-- name: find-user
-- retrieve a user given the email
SELECT * FROM users
WHERE email = :email

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id
