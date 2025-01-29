document.addEventListener('DOMContentLoaded', function () {
    // Star Animation Script
const starContainer = document.getElementById('stars');

// Create random stars
function createStars() {
    const starCount = 100; // Number of stars
    for (let i = 0; i < starCount; i++) {
        const star = document.createElement('div');
        star.classList.add('star');

        // Random positions and animation delay
        const x = Math.random() * 100; // Percent
        const y = Math.random() * -100; // Start above view
        const delay = Math.random() * 5; // Seconds

        star.style.left = `${x}%`;
        star.style.top = `${y}vh`;
        star.style.animationDelay = `${delay}s`;

        starContainer.appendChild(star);
    }
}

// Call the function on page load
createStars();

    // Theme toggling
    const themeButton = document.getElementById('themeButton');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (prefersDark) {
        document.documentElement.setAttribute('data-theme', 'dark');
        themeButton.textContent = '☀️';
    }

    themeButton.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', newTheme);
        themeButton.textContent = newTheme === 'dark' ? '☀️' : '🌙';
        showStatus(`Theme switched to ${newTheme} mode.`, false, true);
    });

    // Multi-step form handling
    const sections = document.querySelectorAll('.section');
    const progressBar = document.getElementById('progressBar');
    const prevButton = document.getElementById('prevButton');
    const nextButton = document.getElementById('nextButton');
    const submitButton = document.getElementById('submitButton');
    let currentSection = 0;

    function updateProgress() {
        const progress = ((currentSection + 1) / sections.length) * 100;
        progressBar.style.width = `${progress}%`;
    }

    function showSection(index) {
        sections.forEach((section, i) => {
            section.classList.toggle('active', i === index);
        });
    
        prevButton.disabled = index === 0;
        nextButton.classList.toggle('hidden', index === sections.length - 1);
        submitButton.classList.toggle('hidden', index !== sections.length - 1);
    
        updateProgress();
    
        // Clear the status message when navigating to a different section
        const statusMessage = document.getElementById('statusMessage');
        statusMessage.classList.add('hidden');
        statusMessage.textContent = '';
    }
    

    prevButton.addEventListener('click', () => {
        if (currentSection > 0) {
            currentSection--;
            showSection(currentSection);
        }
    });

    nextButton.addEventListener('click', () => {
        if (currentSection < sections.length - 1) {
            if (validateSection(currentSection)) {
                currentSection++;
                showSection(currentSection);
            }
        }
    });

    function validateSection(sectionIndex) {
        const section = sections[sectionIndex];
        const inputs = section.querySelectorAll('input[required], textarea[required]');
        let isValid = true;
    
        // Special validation logic for Section 1 (Connection Details)
        if (sectionIndex === 0) {
            const connectionString = document.getElementById('connectionString').value.trim();
            const sasToken = document.getElementById('sasToken').value.trim();
    
            if (!connectionString && !sasToken) {
                isValid = false;
                showStatus('Please provide either the Connection String or SAS Token.', true);
            }
        }
    
        // General validation for all required fields
        inputs.forEach(input => {
            if (!input.value.trim()) {
                isValid = false;
                showInputError(input, 'This field is required.');
            } else {
                removeInputError(input);
            }
        });
    
        if (!isValid && sectionIndex !== 0) {
            showStatus('Please fill in all required fields before proceeding.', true);
        }
    
        return isValid;
    }
    

    function showInputError(input, message) {
        input.classList.add('error');
        input.style.borderColor = 'var(--error-color)';
        let errorContainer = input.nextElementSibling;

        if (!errorContainer || !errorContainer.classList.contains('error-message')) {
            errorContainer = document.createElement('div');
            errorContainer.className = 'error-message';
            input.parentNode.insertBefore(errorContainer, input.nextSibling);
        }

        errorContainer.textContent = message;
    }

    function removeInputError(input) {
        input.classList.remove('error');
        input.style.borderColor = '';
        const errorContainer = input.nextElementSibling;

        if (errorContainer && errorContainer.classList.contains('error-message')) {
            errorContainer.remove();
        }
    }

    // Password visibility toggle
    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', () => {
            const input = button.previousElementSibling;
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);
            button.textContent = type === 'password' ? '👁️' : '👁️‍🗨️';
        });
    });

    // Form submission
    const resetForm = document.getElementById('resetForm');

    function showStatus(message, isError, isModeMessage=false) {
        const statusMessage = document.getElementById('statusMessage');
        statusMessage.classList.remove('hidden', 'error', 'success');
        statusMessage.classList.add(isError ? 'error' : 'success');
        statusMessage.textContent = message;
    
        // Auto-hide only for theme mode messages
        if (isModeMessage) {
            setTimeout(() => {
                statusMessage.classList.add('hidden');
            }, 3000);
        }
    }

    function setLoading(isLoading) {
        const buttonContent = document.getElementById('buttonContent');

        submitButton.disabled = isLoading;
        buttonContent.innerHTML = isLoading
            ? `<svg class="icon spinner" viewBox="0 0 24 24">
                    <circle class="spinner-circle" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
               </svg> Processing...`
            : `<svg class="icon" viewBox="0 0 24 24">
                    <path d="M20 6L9 17l-5-5"/>
               </svg> Submit Request`;
    }

    resetForm.addEventListener('submit', function (event) {
        event.preventDefault();

        if (!validateSection(currentSection)) {
            return;
        }

        const formData = {
            connectionString: document.getElementById('connectionString').value.trim(),
            sasToken: document.getElementById('sasToken').value.trim(),
            containerName: document.getElementById('containerName').value.trim(),
            blobName: document.getElementById('blobName').value.trim(),
            adminName: document.getElementById('adminName').value.trim(),
            adminPassword: document.getElementById('adminPassword').value.trim(),
            tenantName: document.getElementById('tenantName').value.trim(),
            tenantPassword: document.getElementById('tenantPassword').value.trim(),
            mailIds: document.getElementById('mailIds').value.trim().split(',').map(email => email.trim()),
            schema: document.getElementById('schemas').value.trim(), // Add schema here
            downloadPath: document.getElementById('downloadPath').value.trim() // Add download path here
        };

        if (!formData.connectionString && !formData.sasToken) {
            showStatus('Please provide either the Connection String or SAS Token.', true);
            return;
        }

        const formattedConnectionString = formData.connectionString.includes('DefaultEndpointsProtocol=')
            ? formData.connectionString
            : `DefaultEndpointsProtocol=https;AccountName=stibo;AccountKey=${formData.connectionString};EndpointSuffix=core.windows.net`;

        formData.connectionString = formattedConnectionString;

        setLoading(true);

        fetch('http://localhost:8080/api/reset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData),
        })
            .then(response => {
                setLoading(false);
                if (response.ok) {
                    return response.text().then(message => {
                        showStatus(message, false); // Success
                    });
                } else if (response.status === 400) {
                    return response.text().then(message => {
                        showStatus(`Validation Error: ${message}`, true); // Bad Request
                    });
                } else if (response.status === 500) {
                    return response.text().then(message => {
                        showStatus(`Server Error: ${message}`, true); // Internal Server Error
                    });
                } else {
                    showStatus('Unexpected response from the server.', true); // Fallback
                }
            })
            .catch(error => {
                setLoading(false);
                showStatus('Network error. Please check your connection and try again.', true);
            });
    });
    

    // Initialize form
    showSection(0);
});
