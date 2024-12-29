window.onload = function() {
    console.log('Window loaded, fetching user info...');
    fetch('/api/user/info')
        .then(response => {
            console.log('User info response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('User info received:', data);
            const userRole = data.roles[0].replace('ROLE_', '');
            document.querySelectorAll('[data-role]').forEach(element => {
                const allowedRoles = element.dataset.role.split(',');
                console.log('Checking role:', userRole, 'against allowed roles:', allowedRoles);
                if (allowedRoles.includes(userRole)) {
                    element.style.display = 'block';
                } else {
                    element.style.display = 'none';
                }
            });
        })
        .catch(error => {
            console.error('Error fetching user info:', error);
            document.body.insertAdjacentHTML('afterbegin',
                '<div class="alert alert-danger">Error loading user information. Please refresh the page.</div>'
            );
        });
}

function formatJSON(data) {
    try {
        if (typeof data === 'string') {
            data = JSON.parse(data);
        }
        return JSON.stringify(data, null, 2);
    } catch (e) {
        return data;
    }
}

function getArticleCitationsByYear() {
    const id = document.getElementById('citationId').value;
    const year = document.getElementById('citationYear').value;
    fetch(`/api/articles/${id}/citations/${year}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('citationsResult').innerHTML =
                `<pre>Citations for article ${id} in ${year}: ${data}</pre>`;
        })
        .catch(error => {
            document.getElementById('citationsResult').innerHTML =
                `<pre>Error: ${error.message}</pre>`;
        });
}

function addArticleAndUpdateIF() {
    const dateInput = document.getElementById('articleDate').value;
    const date = new Date(dateInput);
    const isoDate = date.toISOString();

    const article = {
        id: parseInt(document.getElementById('articleId').value),
        title: document.getElementById('articleTitle').value,
        pub_model: "Print",
        journal: {
            id: document.getElementById('journalId').value.toString(),
            title: document.getElementById('articleJournal').value
        },
        created: isoDate,
        completed: isoDate,
        authors: [],
        keywords: [],
        references: [],
        article_ids: [],
        publication_types: [],
        grants: []
    };

    fetch('/api/articles/impact-factor', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(article)
    })
    .then(response => response.json())
    .then(impactFactor => {
        document.getElementById('addArticleResult').innerHTML =
            `<pre>${impactFactor === 0.0 ? 'Error: Impact factor calculation failed' :
            `Impact Factor: ${impactFactor.toFixed(3)}`}</pre>`;
    })
    .catch(error => {
        document.getElementById('addArticleResult').innerHTML =
            `<pre>Error: ${error.message}</pre>`;
    });
}

function getArticlesByAuthorSortedByCitations() {
    const author = {
        last_name: document.getElementById('authorLastName').value,
        fore_name: document.getElementById('authorForeName').value,
        initials: document.getElementById('authorInitials').value
    };

    fetch('/api/authors/articles', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(author)
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('authorArticlesResult').innerHTML =
            `<pre>Articles: ${formatJSON(data)}</pre>`;
    })
    .catch(error => {
        document.getElementById('authorArticlesResult').innerHTML =
            `<pre>Error: ${error.message}</pre>`;
    });
}

function getJournalWithMostArticlesByAuthor() {
    const author = {
        last_name: document.getElementById('journalAuthorLastName').value,
        fore_name: document.getElementById('journalAuthorForeName').value,
        initials: document.getElementById('journalAuthorInitials').value
    };

    fetch('/api/authors/journal', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(author)
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById('authorJournalResult').innerHTML =
            `<pre>Most published in: ${data}</pre>`;
    })
    .catch(error => {
        document.getElementById('authorJournalResult').innerHTML =
            `<pre>Error: ${error.message}</pre>`;
    });
}

function getKeyword() {
    const keyword = document.getElementById('keywordInput').value;
    fetch(`/api/keywords/articles/${keyword}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('keywordResult').innerHTML =
                `<pre>Articles with keyword "${keyword}": ${formatJSON(data)}</pre>`;
        })
        .catch(error => {
            document.getElementById('keywordResult').innerHTML =
                `<pre>Error: ${error.message}</pre>`;
        });
}

function getImpactFactor() {
    const journal = document.getElementById('journalTitle').value;
    const year = document.getElementById('journalYear').value;
    fetch(`/api/journals/impact-factor?title=${encodeURIComponent(journal)}&year=${year}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('journalResult').innerHTML =
                `<pre>Impact Factor: ${data}</pre>`;
        })
        .catch(error => {
            document.getElementById('journalResult').innerHTML =
                `<pre>Error: ${error.message}</pre>`;
        });
}

function updateJournalName() {
    const data = {
        journal_id: document.getElementById('oldJournalId').value,
        journal_title: document.getElementById('oldJournalName').value,
        year: parseInt(document.getElementById('updateYear').value),
        newName: document.getElementById('newJournalName').value,
        newId: document.getElementById('newJournalId').value
    };

    fetch('/api/journals/update', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('updateJournalResult').innerHTML =
            `<pre>Update ${data ? 'successful' : 'failed'}</pre>`;
    })
    .catch(error => {
        document.getElementById('updateJournalResult').innerHTML =
            `<pre>Error: ${error.message}</pre>`;
    });
}

function getCountryFundPapers() {
    const country = document.getElementById('countryName').value;
    fetch(`/api/grants/papers/${country}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('countryPapersResult').innerHTML =
                `<pre>Papers funded by ${country}: ${formatJSON(data)}</pre>`;
        })
        .catch(error => {
            document.getElementById('countryPapersResult').innerHTML =
                `<pre>Error: ${error.message}</pre>`;
        });
}